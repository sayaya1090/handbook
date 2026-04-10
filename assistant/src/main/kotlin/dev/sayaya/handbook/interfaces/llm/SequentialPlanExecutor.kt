package dev.sayaya.handbook.interfaces.llm

import dev.sayaya.handbook.domain.AgentCommand
import dev.sayaya.handbook.domain.CommandType
import dev.sayaya.handbook.domain.ExecutionPlan
import dev.sayaya.handbook.usecase.PlanExecutor
import reactor.core.publisher.Flux

/**
 * 실행 계획의 단계를 순서대로 실행하여 AgentCommand 스트림을 생성하는 PlanExecutor 구현체.
 * 각 단계마다 PROGRESS 커맨드를 먼저 발행하고, 해당 단계의 커맨드를 발행한다.
 * 모든 단계 완료 후 COMPLETE 커맨드를 발행한다.
 */
class SequentialPlanExecutor : PlanExecutor {

    override fun execute(plan: ExecutionPlan): Flux<AgentCommand> {
        val totalSteps = plan.steps.size
        val stepCommands = plan.steps
            .sortedBy { it.order }
            .flatMapIndexed { index, step ->
                val progress = AgentCommand(
                    type = CommandType.PROGRESS,
                    payload = mapOf(
                        "current" to (index + 1),
                        "total" to totalSteps,
                        "description" to step.description,
                    ),
                )
                listOf(progress, step.command)
            }
        val complete = AgentCommand(
            type = CommandType.COMPLETE,
            payload = mapOf("intent" to plan.intent),
        )
        return Flux.fromIterable(stepCommands + complete)
    }
}
