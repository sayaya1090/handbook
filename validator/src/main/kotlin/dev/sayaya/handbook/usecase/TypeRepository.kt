package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Type
import reactor.core.publisher.Mono
import java.util.UUID
import kotlin.time.Instant

interface TypeRepository {
    fun cache(workspace: UUID, type: Type): Mono<Void>
    fun find(workspace: UUID, id: String, version: String): Mono<Void>
}