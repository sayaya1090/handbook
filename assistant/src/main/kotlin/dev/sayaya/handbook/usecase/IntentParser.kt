package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.ExecutionPlan
import reactor.core.publisher.Mono

/**
 * 자연어 메시지를 파싱하여 실행 계획을 생성하는 포트.
 *
 * <p><b>책임:</b> LLM 클라이언트를 사용하여 자연어 의도를 분석하고
 * ExecutionPlan(서브 에이전트 정의 포함)으로 변환한다.</p>
 *
 * <p><b>주의:</b> context 파라미터가 주어지면 서브 에이전트 역할/태스크 및
 * 상위 아티팩트 정보를 LLM에 추가 컨텍스트로 전달한다.</p>
 */
interface IntentParser {
    fun parse(userMessage: String, context: String? = null): Mono<ExecutionPlan>
}
