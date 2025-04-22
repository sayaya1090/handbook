package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Workspace
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class WorkspaceService(
    private val repo: WorkspaceRepository,
    private val eventHandler: ExternalServiceHandler,
) {
    fun save(workspaceBuilder: WorkspaceBuilder): Mono<Workspace> = workspaceBuilder.build()
        .let(repo::save)
        .delayUntil(eventHandler::publish)
}