package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Artifact
import java.util.UUID

/**
 * 여러 서브 에이전트의 Artifact를 하나로 병합하는 포트.
 *
 * <p><b>책임:</b> 서브 에이전트들이 각각 생성한 Artifact를 종합하여
 * 부모 실행의 최종 Artifact로 변환한다.</p>
 *
 * <p><b>의존관계:</b> 없음</p>
 *
 * <p><b>주의:</b> 서브 에이전트 이름이 summary에 헤더로 포함되어
 * 각 서브 에이전트의 기여를 추적할 수 있다.</p>
 */
interface ArtifactAggregator {
    /**
     * 서브 에이전트 Artifact 목록을 하나의 Artifact로 병합한다.
     *
     * @param executionId 부모 실행 ID
     * @param intent 부모 실행의 의도 요약
     * @param subArtifacts 서브 에이전트별 Artifact 목록 (이름 → Artifact)
     * @return 병합된 최종 Artifact
     */
    fun aggregate(executionId: UUID, intent: String, subArtifacts: Map<String, Artifact>): Artifact
}
