package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * 서브 에이전트가 포함된 실행 계획의 오케스트레이션을 담당한다.
 *
 * **책임:** 서브 에이전트를 group별로 묶어 순차 실행하되, 같은 group 내 서브 에이전트는
 * 병렬 실행한다. 의존 관계(dependsOn)에 따라 상위 Artifact를 전달하고,
 * 모든 서브 에이전트 완료 후 [ArtifactAggregator]로 결과를 병합한다.
 *
 * **의존관계:**
 * - [SubAgentPlanExecutor] — 서브 에이전트 단일 실행
 * - [ArtifactAggregator] — 서브 에이전트 Artifact 병합
 * - [AgentCommandEventPublisher] — DELEGATE/COMPLETE Kafka 이벤트 발행
 *
 * **주의:** collectedArtifacts는 ConcurrentHashMap으로 관리되어 병렬 그룹 내 스레드 안전성을 보장한다.
 */
class SubAgentOrchestrator(
    private val subAgentExecutor: SubAgentPlanExecutor,
    private val artifactAggregator: ArtifactAggregator,
    private val eventPublisher: AgentCommandEventPublisher,
) {
    /**
     * 서브 에이전트가 포함된 실행 계획을 처리한다.
     * 서브 에이전트를 group별로 묶어 순차적으로 실행하되,
     * 같은 group 내의 서브 에이전트는 Flux.merge로 병렬 실행한다.
     * 의존 관계(dependsOn)에 따라 상위 Artifact를 전달하고,
     * 모든 서브 에이전트 완료 후 ArtifactAggregator로 결과를 병합한다.
     */
    fun execute(
        workspace: UUID,
        executionId: UUID,
        plan: ExecutionPlan,
    ): Mono<Artifact> {
        val collectedArtifacts = ConcurrentHashMap<String, Artifact>()
        val grouped = plan.subAgents.groupBy { it.group }.toSortedMap()
        val seq = AtomicInteger(0)

        var chain: Mono<Void> = Mono.empty()
        for ((groupNumber, agents) in grouped) {
            chain = chain.then(Mono.defer {
                val progressCmd = AgentCommand(
                    type = CommandType.DELEGATE,
                    payload = mapOf(
                        "currentGroup" to groupNumber,
                        "totalGroups" to grouped.size,
                        "subAgents" to agents.map { it.name },
                    ),
                )
                eventPublisher.publish(workspace, seq.incrementAndGet(), progressCmd)

                val monos = agents.map { definition ->
                    val upstream = definition.dependsOn
                        .mapNotNull { dep -> collectedArtifacts[dep]?.let { dep to it } }
                        .toMap()
                    subAgentExecutor.execute(workspace, executionId, definition, upstream)
                        .doOnNext { artifact -> collectedArtifacts[definition.name] = artifact }
                }
                Flux.merge(monos).then()
            })
        }

        return chain.then(Mono.fromCallable {
            val completeCmd = AgentCommand(
                type = CommandType.COMPLETE,
                payload = mapOf("intent" to plan.intent, "subAgentCount" to plan.subAgents.size),
            )
            eventPublisher.publish(workspace, seq.incrementAndGet(), completeCmd)
            artifactAggregator.aggregate(executionId, plan.intent, collectedArtifacts.toMap())
        })
    }
}
