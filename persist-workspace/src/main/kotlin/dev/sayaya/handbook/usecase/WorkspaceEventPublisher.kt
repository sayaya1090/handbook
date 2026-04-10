package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Workspace
import reactor.core.publisher.Mono

interface WorkspaceEventPublisher {
    fun publishCreated(workspace: Workspace): Mono<Void>
    fun publishDeleted(workspaceId: java.util.UUID): Mono<Void>
}
