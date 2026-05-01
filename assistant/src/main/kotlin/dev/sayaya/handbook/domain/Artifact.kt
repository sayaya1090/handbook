package dev.sayaya.handbook.domain

import java.time.Instant
import java.util.*

/**
 * 실행 완료 후 생성되는 결과물.
 *
 * <p><b>책임:</b> 실행 계획의 결과로 발생한 변경 사항을 요약하여 보관한다.
 * AuditEntry에 첨부되어 감사 기록의 일부로 저장된다.</p>
 *
 * @param executionId 실행 고유 식별자
 * @param summary 실행 결과 요약
 * @param changes 개별 변경 사항 목록
 * @param timestamp 아티팩트 생성 시각
 */
data class Artifact(
    val executionId: UUID,
    val summary: String,
    val changes: List<ArtifactChange>,
    val timestamp: Instant = Instant.now(),
)

/**
 * Artifact 내 개별 변경 사항.
 *
 * @param type 변경 유형 (예: NAVIGATE, MUTATE, NOTIFY)
 * @param target 변경 대상
 * @param description 변경 설명
 */
data class ArtifactChange(
    val type: String,
    val target: String,
    val description: String,
)
