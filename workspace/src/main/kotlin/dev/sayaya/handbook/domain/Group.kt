package dev.sayaya.handbook.domain

import java.util.*

/**
 * 특정 워크스페이스에 속한 그룹을 나타내는 엔티티(Entity).
 * 그룹의 동등성은 고유 식별자인 `id`를 기준으로 판단합니다.
 */
@JvmRecord
data class Group (
    val id: UUID,
    val workspace: UUID,
    val name: String,
    val description: String?,
) {
    init {
        require(name.isNotBlank()) { "Name cannot be blank" }
    }

    /**
     * 데이터 클래스의 기본 동작(모든 프로퍼티 비교)을 따르지 않고,
     * 엔티티의 동등성 규칙(ID 기반)을 적용하기 위해 재정의합니다.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Group
        return id == other.id
    }

    /**
     * equals가 id만 비교하므로, hashCode도 id를 기반으로 생성합니다.
     */
    override fun hashCode(): Int {
        return id.hashCode()
    }
}