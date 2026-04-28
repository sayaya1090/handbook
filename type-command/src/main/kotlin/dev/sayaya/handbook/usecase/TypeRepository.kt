package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.domain.TypePatch
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

/**
 * 타입 영속화 포트 (헥사고날 아키텍처 출력 포트).
 *
 * **책임:** 타입의 기간별 조회, 일괄 저장, 부분 수정(속성 upsert), 삭제를 정의한다.
 *
 * **의존관계:**
 * - [R2dbcTypeRepositoryAdapter][dev.sayaya.handbook.interfaces.database.R2dbcTypeRepositoryAdapter] — R2DBC 구현체
 */
interface TypeRepository {
    fun findByWorkspaceAndPeriod(workspace: UUID, effectDateTime: Instant, expireDateTime: Instant): Flux<Type>
    fun save(workspace: UUID, types: List<Type>): Flux<Type>
    fun patch(workspace: UUID, patches: List<TypePatch>): Flux<Type>
    fun delete(workspace: UUID, types: List<Type>): Mono<Void>
}
