package dev.sayaya.handbook.usecase.type

import dev.sayaya.handbook.domain.Type
import reactor.core.publisher.Mono

interface TypeRepository {
    fun save(type: Type): Mono<Type>
}