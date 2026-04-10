package dev.sayaya.handbook.domain

import java.io.Serializable
import java.time.Instant
import java.util.*

/**
 * 문서 검증 워크플로우의 실행 상태를 추적하는 엔티티(Entity).
 *
 * 타입 새 버전 생성 또는 문서 생성 시 생성되며,
 * 검증 완료 시 [Compliance] 결과를 생성한다.
 *
 * @property id 검증 작업 고유 식별자
 * @property workspace 워크스페이스 ID
 * @property documentId 검증 대상 문서 ID
 * @property typeId 검증 기준 타입 ID
 * @property typeVersion 검증 기준 타입 버전
 * @property status 현재 검증 상태
 * @property createdAt 작업 생성 시각
 * @property completedAt 작업 완료 시각 (완료 전이면 null)
 */
@JvmRecord
data class ValidationTask(
    val id: UUID,
    val workspace: UUID,
    val documentId: UUID,
    val typeId: String,
    val typeVersion: String,
    val status: Status,
    val createdAt: Instant,
    val completedAt: Instant?,
) : Serializable {
    init {
        require(status.isTerminal() || completedAt == null) {
            "completedAt must be null when status is not terminal. Current status: $status"
        }
        require(!status.isTerminal() || completedAt != null) {
            "completedAt must not be null when status is terminal. Current status: $status"
        }
    }

    enum class Status {
        NEW,
        PROCESSING,
        DONE,
        FAILED;

        fun isTerminal(): Boolean = this == DONE || this == FAILED
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ValidationTask
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
