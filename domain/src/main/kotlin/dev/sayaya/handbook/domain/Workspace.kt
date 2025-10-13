package dev.sayaya.handbook.domain

import java.util.*

@JvmRecord
data class Workspace (
    val id: UUID,
    val name: String,
    val description: String?,
) {
    init {
        require(name.isNotBlank()) { "Name cannot be blank" }
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Workspace) return false
        return id == other.id
    }
    override fun hashCode(): Int = id.hashCode()
    companion object {
        @JvmRecord
        data class WorkspaceSimple(val id: UUID, val name: String) {
            init {
                require(name.isNotBlank()) { "Name cannot be blank" }
            }
        }
    }
}