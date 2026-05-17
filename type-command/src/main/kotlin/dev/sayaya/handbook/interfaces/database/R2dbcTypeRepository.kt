package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.domain.TypePatch
import dev.sayaya.handbook.usecase.TypeRepository
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

/** Spring Data R2DBC 자동 구현 인터페이스. types 테이블에 대한 기본 CRUD + 기간별 조회를 제공한다. */
interface R2dbcTypeEntityRepository : ReactiveCrudRepository<R2dbcTypeEntity, String> {
    @Query("SELECT * FROM types WHERE id = :id AND version = :version AND workspace = :workspace")
    fun findByPk(id: String, version: String, workspace: UUID): Mono<R2dbcTypeEntity>

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
}

/**
 * [TypeRepository] 포트의 R2DBC 어댑터.
 *
 * **책임:** 타입 도메인 객체의 영속화, 조회, 패치, 삭제를 R2DBC를 통해 수행한다.
 * 속성 엔티티-도메인 매핑은 [AttributeEntityMapper]에 위임한다.
 *
 * **의존관계:**
 * - [R2dbcTypeEntityRepository] — 타입 엔티티 CRUD
 * - [R2dbcAttributeEntityRepository] — 속성 엔티티 CRUD + 이름 기준 삭제
 * - [AttributeEntityMapper] — 속성 엔티티 ↔ 도메인 변환
 * - [DatabaseClient] — 커스텀 SQL (rev 체크 + 증가)
 *
 * **주의:** save()는 기존 속성을 전체 삭제 후 재삽입. patch()는 변경 속성만 이름 기준 삭제 후 삽입 (비충돌 병합).
 * rev 불일치 시 [DuplicateKeyException]으로 409 Conflict 변환.
 */
class R2dbcTypeRepositoryAdapter(
    private val typeRepo: R2dbcTypeEntityRepository,
    private val attrRepo: R2dbcAttributeEntityRepository,
    private val attrMapper: AttributeEntityMapper,
    private val tx: TransactionalOperator,
    private val databaseClient: DatabaseClient,
) : TypeRepository {

    override fun findByWorkspaceAndPeriod(
        workspace: UUID,
        effectDateTime: Instant,
        expireDateTime: Instant,
    ): Flux<Type> {
        return typeRepo.findByWorkspaceAndPeriod(workspace, effectDateTime, expireDateTime)
            .collectList()
            .flatMapMany { entities ->
                if (entities.isEmpty()) return@flatMapMany Flux.empty<Type>()
                val typeIds = entities.map { it.id }
                attrRepo.findByWorkspaceAndTypeIdIn(workspace, typeIds)
                    .collectMultimap { "${it.typeId}:${it.typeVersion}" }
                    .flatMapMany { attrMap ->
                        Flux.fromIterable(entities.map { entity ->
                            val key = "${entity.id}:${entity.version}"
                            val attrs = (attrMap[key] ?: emptyList()).map { attrMapper.toDomain(it) }
                                .sortedBy { it.order() }
                            entity.toDomain(attrs)
                        })
                    }
            }
    }

    override fun save(workspace: UUID, types: List<Type>): Flux<Type> {
        return Flux.fromIterable(types)
            .flatMap { type -> saveOne(workspace, type) }
            .`as`(tx::transactional)
    }

    private fun saveOne(workspace: UUID, type: Type): Mono<Type> {
        val entity = R2dbcTypeEntity.fromDomain(workspace, type)
        val persistenceOp = if (entity.isNew) {
            typeRepo.save(entity)
        } else {
            // 복합키(id, version, workspace) 및 리비전(rev) 동시 체크를 위한 수동 UPDATE
            databaseClient.sql("""
                UPDATE types 
                SET effect_date_time = :effect, expire_date_time = :expire, 
                    description = :desc, primitive = :primitive, parent = :parent, rev = rev + 1
                WHERE id = :id AND version = :version AND workspace = :workspace AND rev = :rev
            """)
                .bind("id", entity.id!!)
                .bind("version", entity.version)
                .bind("workspace", workspace)
                .bind("rev", entity.rev!!)
                .bind("effect", entity.effectDateTime)
                .bind("expire", entity.expireDateTime)
                .bind("desc", entity.description ?: "")
                .bind("primitive", entity.primitive)
                .bind("parent", entity.parent ?: "")
                .fetch().rowsUpdated()
                .flatMap { rowsUpdated ->
                    if (rowsUpdated == 0L) Mono.error(DuplicateKeyException("Version conflict for type ${entity.id}:${entity.version} in workspace $workspace"))
                    else typeRepo.findByPk(entity.id!!, entity.version, workspace) // 업데이트 후 최신 상태 조회 (rev 증가됨)
                }
        }

        return persistenceOp
            .flatMap { saved ->
                attrRepo.deleteByTypeIdAndTypeVersion(type.id(), type.version())
                    .thenReturn(saved)
            }
            .flatMap { saved ->
                val attrEntities = (type.attributes() ?: emptyArray()).map { attr ->
                    attrMapper.toEntity(type.id(), type.version(), workspace, attr)
                }
                if (attrEntities.isEmpty()) Mono.just(saved.toDomain())
                else attrRepo.saveAll(attrEntities)
                    .collectList()
                    .map { attrs -> saved.toDomain(attrs.map { attrMapper.toDomain(it) }) }
            }
    }

    override fun patch(workspace: UUID, patches: List<TypePatch>): Flux<Type> {
        return Flux.fromIterable(patches)
            .flatMap { patch -> patchOne(workspace, patch) }
            .`as`(tx::transactional)
    }

    private fun patchOne(workspace: UUID, patch: TypePatch): Mono<Type> {
        // 1. rev 체크 + description 업데이트 + rev 증가 (Full PK 사용: id, version, workspace)
        val spec = if (patch.description != null) {
            databaseClient.sql("UPDATE types SET rev = rev + 1, description = :desc WHERE id = :id AND version = :version AND workspace = :workspace AND rev = :rev")
                .bind("id", patch.id)
                .bind("version", patch.version)
                .bind("workspace", workspace)
                .bind("rev", patch.rev)
                .bind("desc", patch.description)
        } else {
            databaseClient.sql("UPDATE types SET rev = rev + 1 WHERE id = :id AND version = :version AND workspace = :workspace AND rev = :rev")
                .bind("id", patch.id)
                .bind("version", patch.version)
                .bind("workspace", workspace)
                .bind("rev", patch.rev)
        }
        return spec.fetch().rowsUpdated()
            .flatMap { rowsUpdated ->
                if (rowsUpdated == 0L) return@flatMap Mono.error<Type>(DuplicateKeyException("Version conflict for type ${patch.id}:${patch.version} in workspace $workspace"))
                // 2. 변경 속성만 upsert (이름 기준으로 삭제 후 삽입)
                val attrOps = Flux.fromIterable(patch.attributes)
                    .flatMap { attr ->
                        attrRepo.deleteByTypeIdAndTypeVersionAndName(patch.id, patch.version, attr.name())
                            .then(attrRepo.save(attrMapper.toEntity(patch.id, patch.version, workspace, attr)))
                    }
                // 3. 전체 타입 조회하여 반환
                attrOps.then(
                    typeRepo.findByPk(patch.id, patch.version, workspace).flatMap { entity ->
                        attrRepo.findByTypeIdAndTypeVersion(patch.id, patch.version)
                            .collectList()
                            .map { attrs -> entity.toDomain(attrs.map { attrMapper.toDomain(it) }.sortedBy { it.order() }) }
                    }
                )
            }
    }

    override fun delete(workspace: UUID, types: List<Type>): Mono<Void> {
        return Flux.fromIterable(types)
            .flatMap { type ->
                attrRepo.deleteByTypeIdAndTypeVersion(type.id(), type.version())
                    .then(
                        databaseClient.sql("DELETE FROM types WHERE id = :id AND version = :version AND workspace = :workspace")
                            .bind("id", type.id())
                            .bind("version", type.version())
                            .bind("workspace", workspace)
                            .fetch().rowsUpdated()
                    )
            }
            .`as`(tx::transactional)
            .then()
    }
}
