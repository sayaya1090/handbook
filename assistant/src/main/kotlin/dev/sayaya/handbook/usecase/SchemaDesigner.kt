package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.AgentCommand
import reactor.core.publisher.Flux

/**
 * 스키마 설계 관련 에이전트 기능 포트.
 * 자연어 설명을 기반으로 스키마 변경 커맨드를 생성한다.
 */
interface SchemaDesigner {
    fun design(description: String): Flux<AgentCommand>
}
