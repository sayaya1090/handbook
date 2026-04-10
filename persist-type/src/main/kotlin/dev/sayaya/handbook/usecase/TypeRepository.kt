package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.domain.TypePatch
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

/** 타입 영속화 포트. */
interface TypeRepository {
    fun findByWorkspaceAndPeriod(workspace: UUID, effectDateTime: Instant, expireDateTime: Instant): Flux<Type>
    fun save(workspace: UUID, types: List<Type>): Flux<Type>
    fun patch(workspace: UUID, patches: List<TypePatch>): Flux<Type>
    fun delete(workspace: UUID, types: List<Type>): Mono<Void>
}
