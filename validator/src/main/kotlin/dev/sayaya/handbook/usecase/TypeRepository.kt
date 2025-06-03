package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Type
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.UUID

interface TypeRepository {
    fun cache(workspace: UUID, type: Type): Mono<Void>
    fun find(workspace: UUID, id: String, effectDateTime: Instant, expireDateTime: Instant): Mono<Type>
}