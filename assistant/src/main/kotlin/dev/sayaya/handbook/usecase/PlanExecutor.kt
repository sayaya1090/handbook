package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.AgentCommand
import dev.sayaya.handbook.domain.ExecutionPlan
import reactor.core.publisher.Flux

/**
 * 실행 계획을 순서대로 실행하여 커맨드 스트림을 생성하는 포트.
 * 구현체는 내부 API 클라이언트를 사용하여 각 단계를 실행한다.
 */
interface PlanExecutor {
    fun execute(plan: ExecutionPlan): Flux<AgentCommand>
}
