package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Workspace
import reactor.core.publisher.Mono

interface ExternalService {
    fun publish(workspace: Workspace): Mono<Void>
}