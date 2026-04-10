package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Type
import reactor.core.publisher.Flux
import java.time.Instant
import java.util.*

class TypeService(private val repo: TypeRepository) {
    fun findByRange(workspace: UUID, effectDateTime: Instant?, expireDateTime: Instant?): Flux<Type> =
        if (effectDateTime != null) repo.findByRange(workspace, effectDateTime, expireDateTime ?: effectDateTime)
        else repo.findAll(workspace)
}
