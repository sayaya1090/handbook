package dev.sayaya.handbook.domain

import java.util.*

@JvmRecord
data class Workspace (
    val id: UUID,
    val name: String,
    val description: String?,
) {
    companion object {
        @JvmRecord
        data class Simple(val id: UUID, val name: String)
    }
}