package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Type
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

/**
 * 타입 읽기 전용 조회 포트 (CQRS query-side).
 *
 * **책임:** 전체 조회, 기간별 조회, 특정 버전 조회를 정의한다.
 * persist-type 모듈의 CUD 포트와 네임스페이스 충돌을 방지하기 위해 "Search" 접두사를 사용한다.
 */
interface TypeSearchRepository {
    fun findAll(workspace: UUID): Flux<Type>
    fun findByRange(workspace: UUID, effectDateTime: Instant, expireDateTime: Instant): Flux<Type>
    fun findByIdAndVersion(workspace: UUID, typeId: String, version: String): Mono<Type>

    /**
     * 특정 타입의 모든 버전을 조회한다.
     *
     * @param workspace 워크스페이스 ID
     * @param typeId 타입 ID
     * @return 해당 타입의 모든 버전 목록 (effectDateTime 기준 정렬)
     */
    fun findVersions(workspace: UUID, typeId: String): Flux<Type>
}
