package dev.sayaya.handbook.domain

import java.io.Serializable
import java.time.Instant
import java.util.Objects

/**
 * 시스템에서 사용되는 데이터의 '타입'을 정의하는 엔티티(Entity).
 *
 * 이 엔티티의 고유 식별자는 사람이 읽을 수 있는 `id`와 `version`의 조합(복합키)입니다.
 * 예를 들어, 'post-v1'과 'post-v2'는 서로 다른 타입입니다.
 */
@JvmRecord
data class Type (
    val id: String,
    val version: String,
    val effectDateTime: Instant,
    val expireDateTime: Instant,
    val description: String?,
    val primitive: Boolean,
    // val attributes: List<Attribute> = emptyList(),
    // 부모 타입의 ID를 값으로 직접 참조하여 다른 Aggregate 간의 느슨한 결합을 유지합니다.
    val parent: String? = null,

    ): Serializable {
    init {
        require(id.isNotBlank()) { "Type id cannot be blank" }
        require(id.matches(Regex("^[a-zA-Z0-9가-힣_-]+$"))) { "Type id can only contain alphabet, 한글, numbers, hyphens, and underscores." }
        require(expireDateTime.isAfter(effectDateTime)) { "Expire date time must be after effect date time" }
    }

    /**
     * 데이터 클래스의 기본 동작을 따르지 않고,
     * 복합키(`id`, `version`) 기반의 동등성 규칙을 적용하기 위해 재정의합니다.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Type
        return id == other.id && version == other.version
    }

    /**
     * equals가 복합키를 사용하므로, hashCode도 복합키를 기반으로 생성합니다.
     */
    override fun hashCode(): Int {
        return Objects.hash(id, version)
    }
}