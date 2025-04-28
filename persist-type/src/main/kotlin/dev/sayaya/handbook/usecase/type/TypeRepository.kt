package dev.sayaya.handbook.usecase.type

import dev.sayaya.handbook.domain.Type
import reactor.core.publisher.Mono
import java.util.*

interface TypeRepository {
    fun saveAll(workspace: UUID, types: List<Type>): Mono<List<Type>>
}