package dev.sayaya.handbook.domain

/**
 * LLM이 정의한 서브 에이전트의 명세.
 *
 * <p><b>책임:</b> 동적 서브 에이전트의 이름, 역할, 수행 태스크, 실행 그룹,
 * 의존 관계를 보유하여 오케스트레이터가 서브 에이전트를 생성·실행할 수 있도록 한다.</p>
 *
 * <p><b>의존관계:</b> 없음 (순수 데이터 클래스)</p>
 *
 * <p><b>주의:</b> dependsOn에 명시된 서브 에이전트의 Artifact가 상위 컨텍스트로 전달된다.
 * 순환 의존은 허용되지 않으며, 오케스트레이터에서 사전 검증해야 한다.</p>
 *
 * @param name 서브 에이전트 고유 이름 (같은 ExecutionPlan 내에서 유일)
 * @param role 서브 에이전트의 역할 설명 (LLM 시스템 프롬프트에 포함)
 * @param task 수행할 구체적 태스크 (LLM 사용자 프롬프트로 전달)
 * @param group 실행 그룹 번호. 같은 그룹의 서브 에이전트는 병렬 실행된다.
 * @param dependsOn 이 서브 에이전트가 의존하는 다른 서브 에이전트 이름 목록
 */
data class SubAgentDefinition(
    val name: String,
    val role: String,
    val task: String,
    val group: Int = 0,
    val dependsOn: List<String> = emptyList(),
)
