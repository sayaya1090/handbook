package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.usecase.TypeSearchRepository
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

/**
 * Spring Data R2DBC 자동 구현 인터페이스 — `types` 테이블 읽기 전용 쿼리.
 */
interface R2dbcTypeEntityRepository : ReactiveCrudRepository<R2dbcTypeEntity, String> {
    fun findByWorkspace(workspace: UUID): Flux<R2dbcTypeEntity>

    @Query("""
        SELECT * FROM types
        WHERE workspace = :workspace
          AND effect_date_time < :expireDateTime
          AND expire_date_time > :effectDateTime
    """)
    fun findByWorkspaceAndPeriod(
        workspace: UUID,
        effectDateTime: Instant,
        expireDateTime: Instant,
    ): Flux<R2dbcTypeEntity>

    @Query("""
        SELECT * FROM types
        WHERE workspace = :workspace AND id = :id AND version = :version
        LIMIT 1
    """)
    fun findByWorkspaceAndIdAndVersion(
        workspace: UUID,
        id: String,
        version: String,
    ): Mono<R2dbcTypeEntity>

    @Query("""
        SELECT * FROM types
        WHERE workspace = :workspace AND id = :id
        ORDER BY effect_date_time ASC
    """)
    fun findVersionsByWorkspaceAndId(
        workspace: UUID,
        id: String,
    ): Flux<R2dbcTypeEntity>
}

/**
 * [TypeSearchRepository] 포트의 R2DBC 읽기 전용 어댑터.
 *
 * **책임:** 워크스페이스별 타입 정보 조회 및 특정 ID/버전의 타입을 조회한다.
 * 속성 정보(type_attributes)는 벌크 조회 후 메모리에서 매핑(hydrate)하여 반환한다.
 *
 * **의존관계:**
 * - [R2dbcTypeEntityRepository] — `types` 테이블 조회
 * - [R2dbcAttributeEntityRepository] — `type_attributes` 테이블 조회
 * - [AttributeEntityMapper] — 속성 엔티티 ↔ 도메인 변환
 *
 * **주의:** `hydrate` 메서드는 타입 목록과 해당 타입들의 모든 속성을 한 번에 조회하여 매핑하므로 N+1 문제를 방지한다.
 */
class R2dbcTypeSearchRepositoryAdapter(
    private val typeRepo: R2dbcTypeEntityRepository,
    private val attrRepo: R2dbcAttributeEntityRepository,
    private val attrMapper: AttributeEntityMapper,
) : TypeSearchRepository {

    override fun findAll(workspace: UUID): Flux<Type> =
        hydrate(workspace, typeRepo.findByWorkspace(workspace))

    override fun findByRange(workspace: UUID, effectDateTime: Instant, expireDateTime: Instant): Flux<Type> =
        hydrate(workspace, typeRepo.findByWorkspaceAndPeriod(workspace, effectDateTime, expireDateTime))

    override fun findByIdAndVersion(workspace: UUID, typeId: String, version: String): Mono<Type> =
        typeRepo.findByWorkspaceAndIdAndVersion(workspace, typeId, version)
            .flatMap { entity ->
                attrRepo.findByWorkspaceAndTypeIdIn(workspace, listOf(entity.id))
                    .collectList()
                    .map { attrs ->
                        entity.toDomain(
                            attrs.filter { it.typeVersion == entity.version }
                                .map { attrMapper.toDomain(it) }
                                .sortedBy { it.order() }
                        )
                    }
            }

    override fun findVersions(workspace: UUID, typeId: String): Flux<Type> =
        hydrate(workspace, typeRepo.findVersionsByWorkspaceAndId(workspace, typeId))

    internal fun hydrate(workspace: UUID, entities: Flux<R2dbcTypeEntity>): Flux<Type> =
        entities.collectList().flatMapMany { list ->
            if (list.isEmpty()) return@flatMapMany Flux.empty<Type>()
            val typeIds = list.map { it.id }.distinct()
            attrRepo.findByWorkspaceAndTypeIdIn(workspace, typeIds)
                .collectMultimap { "${it.typeId}:${it.typeVersion}" }
                .flatMapMany { attrMap ->
                    Flux.fromIterable(list.map { entity ->
                        val key = "${entity.id}:${entity.version}"
                        val attrs = (attrMap[key] ?: emptyList())
                            .map { attrMapper.toDomain(it) }
                            .sortedBy { it.order() }
                        entity.toDomain(attrs)
                    })
                }
        }
}
