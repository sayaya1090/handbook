package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.ExecutionPlan
import reactor.core.publisher.Mono

/**
 * 자연어 메시지를 파싱하여 실행 계획을 생성하는 포트.
 * 구현체는 LLM 클라이언트를 사용하여 의도를 분석한다.
 */
interface IntentParser {
    fun parse(userMessage: String): Mono<ExecutionPlan>
}
