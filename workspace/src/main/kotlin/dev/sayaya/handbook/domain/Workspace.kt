package dev.sayaya.handbook.domain

import java.util.*

/**
 * 워크스페이스를 나타내는 엔티티(Entity).
 * 엔티티의 동등성은 고유 식별자인 `id`를 기준으로 판단합니다.
 */
@JvmRecord
data class Workspace (
    val id: UUID,
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
        other as Workspace
        return id == other.id
    }
    /**
     * equals가 id만 비교하므로, hashCode도 id를 기반으로 생성합니다.
     */
    override fun hashCode(): Int = id.hashCode()
    companion object {
        /**
         * 다른 컨텍스트(e.g., User 엔티티)에서 워크스페이스의 요약 정보를 참조할 때 사용하는 값 객체(Value Object).
         * 값 객체의 동등성은 모든 프로퍼티(`id`, `name`)가 같아야 하므로,
         * 데이터 클래스의 기본 `equals`/`hashCode` 동작을 그대로 사용합니다.
         */
        @JvmRecord
        data class WorkspaceSimple(val id: UUID, val name: String) {
            init {
                require(name.isNotBlank()) { "Name cannot be blank" }
            }
            /**
             * 이 요약 객체(WorkspaceSimple)가 주어진 Workspace 엔티티를 나타내는지 확인합니다.
             * 타입과 상관없이 ID만으로 동일한 워크스페이스인지를 판단합니다.
             * @param workspace 비교할 전체 Workspace 엔티티
             * @return ID가 같으면 true
             */
            fun isFor(workspace: Workspace): Boolean = this.id == workspace.id
        }
    }
}