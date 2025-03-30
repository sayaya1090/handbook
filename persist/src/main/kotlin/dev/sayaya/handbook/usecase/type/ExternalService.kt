package dev.sayaya.handbook.usecase.type

import dev.sayaya.handbook.domain.Type
import reactor.core.publisher.Mono

interface ExternalService {
    fun publish(type: Type): Mono<Void>
}