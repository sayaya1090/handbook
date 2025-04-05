package dev.sayaya.handbook.usecase.type

import dev.sayaya.handbook.domain.Type
import reactor.core.publisher.Mono
import java.util.UUID

interface ExternalService {
    fun publish(workspace: UUID, type: Type): Mono<Void>
}