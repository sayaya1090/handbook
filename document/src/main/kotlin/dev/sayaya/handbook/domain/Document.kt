package dev.sayaya.handbook.domain

import java.io.Serializable
import java.time.Instant
import java.util.*

/**
 * 시스템에서 사용되는 일반적인 문서를 나타내는 엔티티(Entity).
 *
 * 이 엔티티는 아직 영속화되지 않은 상태(id가 null)를 표현할 수 있습니다.
 * 엔티티의 동등성은 영속화된 후 할당되는 고유 식별자 `id`를 기준으로 판단합니다.
 */
@JvmRecord
data class Document (
    val id: UUID?,
    val type: String,
    val serial: String,
    val effectDateTime: Instant,
    val expireDateTime: Instant,
    val createDateTime: Instant?,
    val creator: String?,
    val data: Map<String, String?>,
): Serializable {
    init {
        require(serial.matches(Regex("^[a-zA-Z0-9-_]+$"))) { "Document serial must be alphanumeric and may include hyphens and underscores." }
        require(expireDateTime.isAfter(effectDateTime)) { "Expire date time must be after effect date time" }
        // ID가 있다는 것은 영속화되었음을 의미하므로, 생성 관련 메타데이터가 반드시 존재해야 함을 보장합니다.
        require(id == null || (createDateTime != null && creator != null)) { "If id is not null, createDateTime and creator must be not null" }
    }
    /**
     * 엔티티의 동등성 규칙을 적용하기 위해 재정의합니다.
     * - ID가 있는 객체: ID를 기준으로 비교합니다.
     * - ID가 없는 객체(영속화되지 않은 객체): 자기 자신 외에는 그 어떤 객체와도 같을 수 없습니다.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Document
        // ID가 없는(null) 엔티티는 다른 어떤 객체와도 동일하다고 판단할 수 없습니다.
        if (id == null) return false
        return id == other.id
    }

    /**
     * equals가 id를 기준으로 동작하므로, hashCode도 id를 기반으로 생성합니다.
     * ID가 없는 객체는 기본 객체의 신원 해시코드(identity hash code)를 사용합니다.
     */
    override fun hashCode(): Int {
        return id?.hashCode() ?: System.identityHashCode(this)
    }
}