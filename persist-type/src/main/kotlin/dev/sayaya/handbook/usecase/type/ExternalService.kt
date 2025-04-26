package dev.sayaya.handbook.usecase.type

import dev.sayaya.handbook.domain.Type
import reactor.core.publisher.Mono
import java.util.*

interface ExternalService {
    fun publish(workspace: UUID, type: Type): Mono<Void>
}