package dev.sayaya.handbook.interfaces.llm

import dev.sayaya.handbook.domain.Artifact
import dev.sayaya.handbook.domain.ArtifactChange
import dev.sayaya.handbook.usecase.ArtifactAggregator
import java.util.*

/**
 * 서브 에이전트 Artifact를 하나의 Artifact로 병합하는 기본 구현체.
 *
 * <p><b>책임:</b> 각 서브 에이전트의 changes를 하나의 리스트로 합치고,
 * summary를 서브 에이전트 이름 헤더와 함께 연결하여 최종 Artifact를 생성한다.</p>
 *
 * <p><b>의존관계:</b> 없음</p>
 *
 * <p><b>주의:</b> 서브 에이전트 간 changes에 동일 target이 있을 수 있으며,
 * 이 경우 중복 제거 없이 모두 포함된다. 충돌 해소는 상위 레이어 책임이다.</p>
 */
class DefaultArtifactAggregator : ArtifactAggregator {

    override fun aggregate(executionId: UUID, intent: String, subArtifacts: Map<String, Artifact>): Artifact {
        val allChanges = mutableListOf<ArtifactChange>()
        val summaryParts = mutableListOf<String>()

        subArtifacts.forEach { (name, artifact) ->
            summaryParts.add("[$name] ${artifact.summary}")
            allChanges.addAll(artifact.changes)
        }

        return Artifact(
            executionId = executionId,
            summary = if (summaryParts.isEmpty()) intent else "$intent\n${summaryParts.joinToString("\n")}",
            changes = allChanges.toList(),
        )
    }
}
