package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.AuditEntry
import dev.sayaya.handbook.domain.ExecutionPlan
import reactor.core.publisher.Mono
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/**
 * AI 어시스턴트 메인 서비스.
 * 자연어 메시지를 파싱하고 실행 계획을 Kafka 이벤트로 발행한다.
 * 에이전트는 제3의 협업자로서 워크스페이스 이벤트 채널을 통해 행동한다.
 */
class AssistantService(
    private val intentParser: IntentParser,
    private val planExecutor: PlanExecutor,
    private val eventPublisher: AgentCommandEventPublisher,
    private val auditRepository: AuditRepository,
) {
    private val currentExecution: AtomicReference<reactor.core.Disposable?> = AtomicReference(null)
    private val currentAuditId: AtomicReference<UUID?> = AtomicReference(null)

    /**
     * 자연어 메시지를 파싱하여 실행 계획을 반환한다.
     * 감사 기록에 REQUESTED 상태로 저장한다.
     */
    fun request(workspace: UUID, message: String): Mono<ExecutionPlan> {
        return intentParser.parse(message).flatMap { plan ->
            val entry = AuditEntry(
                workspace = workspace,
                userMessage = message,
                intent = plan.intent,
                confidence = plan.confidence,
                plan = plan,
                status = AuditEntry.Status.REQUESTED,
            )
            auditRepository.save(entry).doOnNext { saved ->
                currentAuditId.set(saved.id)
            }.thenReturn(plan)
        }
    }

    /**
     * 실행 계획을 실행하고 각 커맨드를 Kafka AGENT_COMMAND 이벤트로 발행한다.
     * event-broadcaster를 통해 워크스페이스의 모든 멤버에게 브로드캐스트된다.
     */
    fun execute(workspace: UUID, plan: ExecutionPlan): Mono<Void> {
        val auditId = currentAuditId.get()
        val updateExecuting = if (auditId != null)
            auditRepository.updateStatus(auditId, AuditEntry.Status.EXECUTING)
        else Mono.empty()

        return updateExecuting.then(Mono.fromRunnable {
            val seq = AtomicInteger(0)
            val disposable = planExecutor.execute(plan)
                .doOnNext { command -> eventPublisher.publish(workspace, seq.incrementAndGet(), command) }
                .doOnComplete {
                    currentExecution.set(null)
                    auditId?.let { auditRepository.updateStatus(it, AuditEntry.Status.COMPLETED).subscribe() }
                    currentAuditId.set(null)
                }
                .doOnError {
                    currentExecution.set(null)
                    currentAuditId.set(null)
                }
                .subscribe()
            currentExecution.set(disposable)
        })
    }

    /**
     * 현재 실행 중인 계획을 취소한다.
     */
    fun abort(): Mono<Void> = Mono.fromRunnable {
        currentExecution.getAndSet(null)?.dispose()
        currentAuditId.getAndSet(null)?.let { id ->
            auditRepository.updateStatus(id, AuditEntry.Status.ABORTED).subscribe()
        }
    }
}
