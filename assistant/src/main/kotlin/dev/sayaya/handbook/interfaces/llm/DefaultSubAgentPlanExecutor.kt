package dev.sayaya.handbook.interfaces.llm

import dev.sayaya.handbook.domain.*
import dev.sayaya.handbook.usecase.AgentCommandEventPublisher
import dev.sayaya.handbook.usecase.IntentParser
import dev.sayaya.handbook.usecase.PlanExecutor
import dev.sayaya.handbook.usecase.SubAgentPlanExecutor
import reactor.core.publisher.Mono
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

/**
 * IntentParser와 PlanExecutor를 조합하여 서브 에이전트를 실행하는 기본 구현체.
 *
 * <p><b>책임:</b> SubAgentDefinition의 role/task와 상위 아티팩트를 컨텍스트로 구성하여
 * IntentParser에 전달하고, 생성된 계획을 PlanExecutor로 실행한 뒤
 * 결과를 Artifact로 수집한다. 실행 중 PROGRESS 이벤트를 서브 에이전트 이름과 함께 발행한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link IntentParser} — 서브 에이전트 태스크를 ExecutionPlan으로 변환</li>
 *   <li>{@link PlanExecutor} — 계획 실행 및 AgentCommand 스트림 생성</li>
 *   <li>{@link AgentCommandEventPublisher} — Kafka 이벤트 발행</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 서브 에이전트가 생성한 ExecutionPlan의 subAgents 필드는 무시되어
 * 최대 중첩 깊이 1이 보장된다.</p>
 */
class DefaultSubAgentPlanExecutor(
    private val intentParser: IntentParser,
    private val planExecutor: PlanExecutor,
    private val eventPublisher: AgentCommandEventPublisher,
) : SubAgentPlanExecutor {

    override fun execute(
        workspace: UUID,
        parentExecutionId: UUID,
        definition: SubAgentDefinition,
        upstreamArtifacts: Map<String, Artifact>,
    ): Mono<Artifact> {
        val context = buildContext(definition, upstreamArtifacts)
        val enrichedPrompt = "[${definition.role}] ${definition.task}"

        return intentParser.parse(enrichedPrompt, context)
            .map { plan -> plan.copy(subAgents = emptyList()) }
            .flatMap { plan -> executePlan(workspace, parentExecutionId, definition.name, plan) }
    }

    private fun buildContext(definition: SubAgentDefinition, upstreamArtifacts: Map<String, Artifact>): String {
        val sb = StringBuilder()
        sb.appendLine("You are a sub-agent with the following role: ${definition.role}")
        sb.appendLine("Your task: ${definition.task}")
        if (upstreamArtifacts.isNotEmpty()) {
            sb.appendLine("\nUpstream artifacts from dependent sub-agents:")
            upstreamArtifacts.forEach { (name, artifact) ->
                sb.appendLine("--- $name ---")
                sb.appendLine("Summary: ${artifact.summary}")
                artifact.changes.forEach { change ->
                    sb.appendLine("  [${change.type}] ${change.target}: ${change.description}")
                }
            }
        }
        sb.appendLine("\nIMPORTANT: Do NOT define subAgents. You are already a sub-agent (max depth = 1).")
        return sb.toString()
    }

    private fun executePlan(
        workspace: UUID,
        parentExecutionId: UUID,
        subAgentName: String,
        plan: ExecutionPlan,
    ): Mono<Artifact> {
        val seq = AtomicInteger(0)
        val changes = mutableListOf<ArtifactChange>()

        return planExecutor.execute(plan)
            .doOnNext { command ->
                val enrichedCommand = if (command.type == CommandType.PROGRESS) {
                    val enrichedPayload = (command.payload ?: emptyMap()) + ("subAgentName" to subAgentName)
                    command.copy(payload = enrichedPayload)
                } else {
                    command
                }
                eventPublisher.publish(workspace, seq.incrementAndGet(), enrichedCommand)

                if (command.type != CommandType.PROGRESS && command.type != CommandType.COMPLETE) {
                    changes.add(
                        ArtifactChange(
                            type = command.type.name,
                            target = command.target ?: "",
                            description = "$subAgentName: ${command.type.name}",
                        )
                    )
                }
            }
            .then(Mono.fromCallable {
                Artifact(
                    executionId = parentExecutionId,
                    summary = "[$subAgentName] ${plan.intent}",
                    changes = changes.toList(),
                )
            })
    }
}
