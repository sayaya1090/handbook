package dev.sayaya.handbook.domain

import java.io.Serializable

/**
 * 역할의 적용 범위를 나타내는 레벨.
 */
enum class RoleLevel {
    SYSTEM, WORKSPACE, TYPE, DOCUMENT
}

/**
 * 역할을 나타내는 값 객체(Value Object).
 * 역할은 이름과 해당 역할에 부여된 Permission 집합으로 구성된다.
 *
 * @property name 역할 이름
 * @property level 역할 적용 범위
 * @property permissions 이 역할에 부여된 권한 집합
 */
@JvmRecord
data class Role(
    val name: String,
    val level: RoleLevel,
    val permissions: Set<Permission>,
) : Serializable {
    init {
        require(name.isNotBlank()) { "Role name cannot be blank" }
    }

    /**
     * 이 역할이 대상 Permission을 포함하는지 확인한다.
     * 역할의 permissions 중 하나라도 대상과 매칭되면 true.
     */
    fun hasPermission(target: Permission): Boolean =
        permissions.any { it.matches(target) }
}
