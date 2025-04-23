package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Workspace
import java.util.*

data class WorkspaceBuilder (
    var id: UUID?=null,
    var name: String,
    var description: String?=null,
){
    fun build() = Workspace (
        id ?: UUID.randomUUID(),
        name,
        description
    )
}