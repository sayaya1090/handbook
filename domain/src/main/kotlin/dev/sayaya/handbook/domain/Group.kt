package dev.sayaya.handbook.domain

import java.util.*

@JvmRecord
data class Group (
    val id: UUID,
    val workspace: UUID,
    val name: String,
    val description: String?,
)