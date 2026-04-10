package dev.sayaya.handbook.domain

import java.io.Serializable
import java.time.Instant
import java.util.*

/**
 * 리소스 변경 이력을 기록하는 감사 로그 엔티티(Entity).
 * 한 번 생성되면 수정되지 않으며 불변으로 보존된다.
 * 동등성은 고유 식별자인 `id`를 기준으로 판단한다.
 *
 * @property id 감사 로그 고유 ID
 * @property workspace 대상 워크스페이스
 * @property userId 변경을 수행한 사용자
 * @property action 수행한 동작 (예: "CREATE", "UPDATE", "DELETE", "BULK_CORRECT")
 * @property resourceType 대상 리소스 타입 (예: "Type", "Document", "Group")
 * @property resourceId 대상 리소스 식별자
 * @property detail 변경 상세 내역 (선택)
 * @property timestamp 변경 시각
 */
@JvmRecord
data class AuditLog(
    val id: UUID,
    val workspace: UUID,
    val userId: UUID,
    val action: String,
    val resourceType: String,
    val resourceId: String,
    val detail: Map<String, Any?>?,
    val timestamp: Instant,
) : Serializable {
    init {
        require(action.isNotBlank()) { "Action cannot be blank" }
        require(resourceType.isNotBlank()) { "Resource type cannot be blank" }
        require(resourceId.isNotBlank()) { "Resource ID cannot be blank" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AuditLog
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
