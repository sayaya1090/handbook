package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * AI 어시스턴트 메인 오케스트레이터.
 *
 * **책임:** 자연어 메시지를 파싱하여 실행 계획을 생성하고,
 * 각 커맨드를 Kafka AGENT_COMMAND 이벤트로 발행한다.
 * 실행 컨텍스트 관리는 [ExecutionContextManager]에 위임하고,
 * 서브 에이전트 오케스트레이션은 [SubAgentOrchestrator]에 위임한다.
 *
 * **의존관계:**
 * - [IntentParser] — 자연어 -> 실행 계획 변환 (LLM)
 * - [PlanExecutor] — 실행 계획 -> AgentCommand 스트림 생성
 * - [AgentCommandEventPublisher] — Kafka 이벤트 발행
 * - [AuditRepository] — 감사 기록 저장
 * - [ExecutionContextManager] — 활성 실행 컨텍스트 생명주기 관리
 * - [SubAgentOrchestrator] — 서브 에이전트 그룹 실행 (nullable, 서브 에이전트 미사용 시 null)
 *
 * **주의:** 실행 완료/에러/취소 시 ExecutionContextManager에서 컨텍스트가 자동 제거된다.
 * 서브 에이전트가 포함된 실행 계획은 SubAgentOrchestrator에 위임된다.
 */
class AssistantService(
    private val intentParser: IntentParser,
    private val planExecutor: PlanExecutor,
    private val eventPublisher: AgentCommandEventPublisher,
    private val auditRepository: AuditRepository,
    private val contextManager: ExecutionContextManager = ExecutionContextManager(),
    private val subAgentOrchestrator: SubAgentOrchestrator? = null,
) {
    /**
     * 이전 버전과의 호환성을 위한 보조 생성자.
     * subAgentExecutor와 artifactAggregator를 개별적으로 받아 SubAgentOrchestrator를 구성한다.
     */
    constructor(
        intentParser: IntentParser,
        planExecutor: PlanExecutor,
        eventPublisher: AgentCommandEventPublisher,
        auditRepository: AuditRepository,
        contextManager: ExecutionContextManager = ExecutionContextManager(),
        subAgentExecutor: SubAgentPlanExecutor? = null,
        artifactAggregator: ArtifactAggregator? = null,
    ) : this(
        intentParser = intentParser,
        planExecutor = planExecutor,
        eventPublisher = eventPublisher,
        auditRepository = auditRepository,
        contextManager = contextManager,
        subAgentOrchestrator = if (subAgentExecutor != null && artifactAggregator != null)
            SubAgentOrchestrator(subAgentExecutor, artifactAggregator, eventPublisher)
        else null,
    )

    /**
     * 자연어 메시지를 파싱하여 실행 계획과 실행 ID를 반환한다.
     * 감사 기록에 REQUESTED 상태로 저장한다.
     * executionId는 auditId와 동일하다.
     */
    fun request(workspace: UUID, message: String): Mono<ExecutionRequest> {
        return intentParser.parse(message).flatMap { plan ->
            val entry = AuditEntry(
                workspace = workspace,
                userMessage = message,
                intent = plan.intent,
                confidence = plan.confidence,
                plan = plan,
                status = AuditEntry.Status.REQUESTED,
            )
            auditRepository.save(entry).map { saved ->
                ExecutionRequest(executionId = saved.id, plan = plan)
            }
        }
    }

    /**
     * 실행 계획을 실행하고 각 커맨드를 Kafka AGENT_COMMAND 이벤트로 발행한다.
     * ExecutionContextManager에 컨텍스트를 등록하고, 완료/에러 시 제거한다.
     */
    fun execute(workspace: UUID, executionId: UUID, plan: ExecutionPlan): Mono<Void> {
        val context = contextManager.register(executionId, workspace, plan)
        val updateExecuting = auditRepository.updateStatus(executionId, AuditEntry.Status.EXECUTING)

        return if (plan.subAgents.isNotEmpty() && subAgentOrchestrator != null) {
            updateExecuting.then(Mono.fromRunnable {
                context.status.set("EXECUTING")
                val disposable = subAgentOrchestrator.execute(workspace, executionId, plan)
                    .doOnSuccess { artifact ->
                        context.status.set("COMPLETED")
                        contextManager.remove(executionId)
                        if (artifact != null) {
                            auditRepository.updateArtifact(executionId, artifact)
                                .then(auditRepository.updateStatus(executionId, AuditEntry.Status.COMPLETED))
                                .subscribe()
                        } else {
                            auditRepository.updateStatus(executionId, AuditEntry.Status.COMPLETED).subscribe()
                        }
                    }
                    .doOnError {
                        context.status.set("ERROR")
                        contextManager.remove(executionId)
                    }
                    .subscribe()
                context.disposable.set(disposable)
            })
        } else {
            updateExecuting.then(Mono.fromRunnable {
                val seq = AtomicInteger(0)
                context.status.set("EXECUTING")
                val disposable = planExecutor.execute(plan)
                    .concatMap { command ->
                        if (command.type == CommandType.PROGRESS) {
                            val payload = command.payload
                            val currentGroup = payload?.get("currentGroup") as? Int ?: 0
                            context.currentGroup.set(currentGroup)
                        }
                        eventPublisher.publish(workspace, seq.incrementAndGet(), command)
                        if (command.type == CommandType.AWAIT_CONFIRM) {
                            waitForResponse(context).flatMap { response ->
                                if (response.equals("cancel", ignoreCase = true)) {
                                    Mono.error(RuntimeException("User cancelled"))
                                } else {
                                    Mono.just(command)
                                }
                            }
                        } else {
                            Mono.just(command)
                        }
                    }
                    .doOnComplete {
                        context.status.set("COMPLETED")
                        contextManager.remove(executionId)
                        val artifact = buildArtifact(executionId, plan)
                        auditRepository.updateArtifact(executionId, artifact)
                            .then(auditRepository.updateStatus(executionId, AuditEntry.Status.COMPLETED))
                            .subscribe()
                    }
                    .doOnError {
                        context.status.set("ERROR")
                        contextManager.remove(executionId)
                    }
                    .subscribe()
                context.disposable.set(disposable)
            })
        }
    }

    /**
     * AWAIT_CONFIRM에 대한 사용자 응답을 특정 실행에 전달한다.
     */
    fun respond(workspace: UUID, executionId: UUID, response: String): Mono<Void> = Mono.fromRunnable {
        contextManager.respondTo(executionId, response)
    }

    private fun waitForResponse(context: ExecutionContext): Mono<String> {
        val sink = Sinks.one<String>()
        context.responseSink.set(sink)
        return sink.asMono()
    }

    /**
     * 특정 실행을 취소한다.
     */
    fun abort(executionId: UUID): Mono<Void> = Mono.fromRunnable {
        val context = contextManager.remove(executionId)
        if (context != null) {
            context.status.set("ABORTED")
            context.disposable.getAndSet(null)?.dispose()
            context.responseSink.getAndSet(null)?.tryEmitError(RuntimeException("Aborted"))
            auditRepository.updateStatus(context.auditId, AuditEntry.Status.ABORTED).subscribe()
        }
    }

    /**
     * 워크스페이스의 활성 실행 목록과 진행 상황을 반환한다.
     */
    fun getExecutions(workspace: UUID): Flux<Map<String, Any>> = Flux.defer {
        val active = contextManager.listByWorkspace(workspace).map { ctx ->
            mapOf<String, Any>(
                "executionId" to ctx.id,
                "intent" to ctx.plan.intent,
                "status" to ctx.status.get(),
                "currentGroup" to ctx.currentGroup.get(),
                "totalGroups" to ctx.totalGroups,
            )
        }
        Flux.fromIterable(active)
    }

    /**
     * 워크스페이스의 완료된 실행에서 생성된 Artifact 목록을 반환한다.
     */
    fun getArtifacts(workspace: UUID): Flux<AuditEntry> {
        return auditRepository.findByWorkspaceAndStatusWithArtifact(workspace, AuditEntry.Status.COMPLETED)
    }

    private fun buildArtifact(executionId: UUID, plan: ExecutionPlan): Artifact {
        val changes = plan.steps
            .filter { it.command.type != CommandType.PROGRESS && it.command.type != CommandType.COMPLETE }
            .map { step ->
                ArtifactChange(
                    type = step.command.type.name,
                    target = step.command.target ?: "",
                    description = step.description,
                )
            }
        return Artifact(
            executionId = executionId,
            summary = plan.intent,
            changes = changes,
        )
    }
}
