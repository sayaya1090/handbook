package dev.sayaya.handbook.interfaces.database

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.AttributeType
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.usecase.TypeRepository
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

interface R2dbcTypeEntityRepository : ReactiveCrudRepository<R2dbcTypeEntity, String> {
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

class R2dbcTypeRepositoryAdapter(
    private val typeRepo: R2dbcTypeEntityRepository,
    private val attrRepo: R2dbcAttributeEntityRepository,
    private val objectMapper: ObjectMapper,
    private val tx: TransactionalOperator,
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
                            val attrs = (attrMap[key] ?: emptyList()).map { it.toDomain() }
                                .sortedBy { it.order }
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
        return typeRepo.save(entity)
            .flatMap { saved ->
                attrRepo.deleteByTypeIdAndTypeVersion(type.id, type.version)
                    .thenReturn(saved)
            }
            .flatMap { saved ->
                val attrEntities = type.attributes.map { attr ->
                    R2dbcAttributeEntity(
                        typeId = type.id,
                        typeVersion = type.version,
                        workspace = workspace,
                        name = attr.name,
                        order = attr.order,
                        description = attr.description,
                        attributeType = objectMapper.writeValueAsString(attr.type),
                        nullable = attr.nullable,
                        inherited = attr.inherited,
                    )
                }
                if (attrEntities.isEmpty()) Mono.just(saved.toDomain())
                else attrRepo.saveAll(attrEntities)
                    .collectList()
                    .map { attrs -> saved.toDomain(attrs.map { it.toDomain() }) }
            }
    }

    override fun delete(workspace: UUID, types: List<Type>): Mono<Void> {
        return Flux.fromIterable(types)
            .flatMap { type ->
                attrRepo.deleteByTypeIdAndTypeVersion(type.id, type.version)
                    .then(typeRepo.delete(R2dbcTypeEntity.fromDomain(workspace, type)))
            }
            .`as`(tx::transactional)
            .then()
    }

    private fun R2dbcAttributeEntity.toDomain(): Attribute = Attribute(
        name = name,
        order = order,
        description = description,
        type = objectMapper.readValue<AttributeType>(attributeType),
        nullable = nullable,
        inherited = inherited,
    )
}
