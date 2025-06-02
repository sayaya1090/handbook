package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Type
import reactor.core.publisher.Mono

interface TypeRepository {
    fun find(): Mono<Type>
}