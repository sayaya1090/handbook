package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.domain.TypePatch
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

/**
 * 타입 CRUD 비즈니스 로직.
 * Spring 어노테이션 없음 — interfaces.config에서 Bean 등록.
 */
class TypeService(
    private val typeRepository: TypeRepository,
    private val eventPublisher: TypeEventPublisher,
) {
    fun findByPeriod(workspace: UUID, effectDateTime: Instant, expireDateTime: Instant): Flux<Type> {
        return typeRepository.findByWorkspaceAndPeriod(workspace, effectDateTime, expireDateTime)
    }

    fun save(workspace: UUID, types: List<Type>): Flux<Type> {
        return typeRepository.save(workspace, types)
            .doOnNext { type -> eventPublisher.publishCreated(workspace, type) }
    }

    fun patch(workspace: UUID, patches: List<TypePatch>): Flux<Type> {
        return typeRepository.patch(workspace, patches)
            .doOnNext { type -> eventPublisher.publishCreated(workspace, type) }
    }

    fun delete(workspace: UUID, types: List<Type>): Mono<Void> {
        return typeRepository.delete(workspace, types)
            .doOnSuccess { types.forEach { type -> eventPublisher.publishDeleted(workspace, type) } }
    }
}
