package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Artifact
import dev.sayaya.handbook.domain.SubAgentDefinition
import reactor.core.publisher.Mono
import java.util.*

/**
 * 서브 에이전트 단일 실행을 담당하는 포트.
 *
 * <p><b>책임:</b> SubAgentDefinition으로부터 서브 에이전트용 실행 계획을 생성하고,
 * PlanExecutor를 통해 실행한 뒤 결과 Artifact를 반환한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link IntentParser} — 서브 에이전트 태스크를 실행 계획으로 변환</li>
 *   <li>{@link PlanExecutor} — 생성된 계획의 실행</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 서브 에이전트는 중첩 깊이 1로 제한된다.
 * 서브 에이전트가 생성한 ExecutionPlan의 subAgents는 무시된다.</p>
 */
interface SubAgentPlanExecutor {
    /**
     * 서브 에이전트를 실행하고 결과 Artifact를 반환한다.
     *
     * @param workspace 워크스페이스 ID
     * @param parentExecutionId 부모 실행 ID (감사 추적 및 이벤트 발행에 사용)
     * @param definition 서브 에이전트 정의
     * @param upstreamArtifacts 의존하는 서브 에이전트들의 완료된 Artifact (이름 → Artifact)
     * @return 서브 에이전트 실행 결과 Artifact
     */
    fun execute(
        workspace: UUID,
        parentExecutionId: UUID,
        definition: SubAgentDefinition,
        upstreamArtifacts: Map<String, Artifact>,
    ): Mono<Artifact>
}
