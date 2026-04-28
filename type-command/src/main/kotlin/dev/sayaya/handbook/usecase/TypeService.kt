package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.domain.TypePatch
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

/**
 * 타입 CRUD 비즈니스 로직 (유스케이스 계층).
 *
 * **책임:** findByPeriod(조회), save(전체 저장), patch(속성 부분 업데이트), delete 실행 후 Kafka 이벤트 발행.
 *
 * **의존관계:**
 * - [TypeRepository] — 영속화 포트 (R2DBC 어댑터가 구현)
 * - [TypeEventPublisher] — Kafka 이벤트 발행 (TYPE_CREATED/DELETED)
 *
 * **주의:** Spring 어노테이션 없음 — interfaces.config.TypeConfig에서 Bean 등록.
 * patch() 시 변경된 속성만 upsert하고 TYPE_CREATED 이벤트를 발행한다.
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
