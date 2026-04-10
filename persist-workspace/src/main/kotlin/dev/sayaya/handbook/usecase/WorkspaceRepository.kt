package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Workspace
import reactor.core.publisher.Mono

interface WorkspaceRepository {
    fun save(workspace: Workspace): Mono<Workspace>
    fun update(workspace: Workspace): Mono<Workspace>
    fun delete(id: java.util.UUID): Mono<Void>
}
