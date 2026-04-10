package dev.sayaya.handbook.interfaces.llm

import dev.sayaya.handbook.domain.AgentCommand
import dev.sayaya.handbook.domain.CommandType
import dev.sayaya.handbook.domain.ExecutionPlan
import dev.sayaya.handbook.usecase.PlanExecutor
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * 실행 계획의 단계를 그룹 단위로 실행하여 AgentCommand 스트림을 생성하는 PlanExecutor 구현체.
 *
 * <p><b>책임:</b> 같은 그룹 번호를 가진 단계들을 Flux.merge()로 병렬 실행하고,
 * 그룹 간에는 concatMap으로 순차 실행한다. 각 그룹 시작 시 PROGRESS 커맨드를 발행하고,
 * 모든 그룹 완료 후 COMPLETE 커맨드를 발행한다.</p>
 *
 * <p><b>의존관계:</b> 없음</p>
 *
 * <p><b>주의:</b> group 필드가 동일한 step들은 동시에 실행되므로,
 * 서로 의존성이 없는 커맨드만 같은 그룹에 배치해야 한다.</p>
 */
class GroupedPlanExecutor : PlanExecutor {

    override fun execute(plan: ExecutionPlan): Flux<AgentCommand> {
        val grouped = plan.steps.groupBy { it.group }.toSortedMap()
        val totalGroups = grouped.size

        val groupFlux = Flux.fromIterable(grouped.entries)
            .concatMap { (groupNumber, steps) ->
                val sortedSteps = steps.sortedBy { it.order }
                val progress = AgentCommand(
                    type = CommandType.PROGRESS,
                    payload = mapOf(
                        "currentGroup" to (grouped.keys.indexOf(groupNumber) + 1),
                        "totalGroups" to totalGroups,
                        "parallel" to (sortedSteps.size > 1),
                        "stepCount" to sortedSteps.size,
                        "descriptions" to sortedSteps.map { it.description },
                    ),
                )
                val commandFlux = Flux.merge(
                    sortedSteps.map { step -> Mono.just(step.command) }
                )
                Flux.concat(Mono.just(progress), commandFlux)
            }

        val complete = AgentCommand(
            type = CommandType.COMPLETE,
            payload = mapOf("intent" to plan.intent),
        )
        return Flux.concat(groupFlux, Mono.just(complete))
    }
}
