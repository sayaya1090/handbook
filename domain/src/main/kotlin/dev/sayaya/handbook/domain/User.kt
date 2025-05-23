package dev.sayaya.handbook.domain

import java.util.*

@JvmRecord
data class User (
    val id: UUID,
    val name: String,
    val workspaces: List<Workspace.Companion.Simple>
)