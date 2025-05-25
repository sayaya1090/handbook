package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Type
import reactor.core.publisher.Flux
import java.time.Instant
import java.util.*

interface TypeRepository {
    fun findAll(workspace: UUID): Flux<Type>
    fun findByRange(workspace: UUID, effectDateTime: Instant, expireDateTime: Instant): Flux<Type>
}