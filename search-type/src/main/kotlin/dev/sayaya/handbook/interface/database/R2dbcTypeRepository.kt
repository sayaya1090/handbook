package dev.sayaya.handbook.`interface`.database

import com.fasterxml.jackson.databind.ObjectMapper
import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.AttributeTypeDefinition
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.domain.exception.MissingFieldException
import dev.sayaya.handbook.usecase.TypeRepository
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@Repository
class R2dbcTypeRepository(private val template: R2dbcEntityTemplate, private val objectMapper: ObjectMapper): TypeRepository {
    override fun findAll(workspace: UUID): Flux<Type> = template.databaseClient.sql(FIND_TYPE_WITH_PREV_AND_NEXT)
        .bind("workspace", workspace)
        .mapValue(R2dbcTypeEntity::class.java)
        .all().collectList()
        .flatMapMany { r2dbcTypes ->
            r2dbcTypes.toDomainWithAttributes(workspace)
        }

    override fun findByRange(workspace: UUID, effectDateTime: Instant, expireDateTime: Instant): Flux<Type> = template.databaseClient.sql(FIND_TYPE_WITH_PREV_AND_NEXT_BY_RANGE).bind("workspace", workspace)
            .bind("effectDateTime", effectDateTime)
            .bind("expireDateTime", expireDateTime)
            .mapValue(R2dbcTypeEntity::class.java)
            .all().collectList()
            .flatMapMany { r2dbcTypes ->
                r2dbcTypes.toDomainWithAttributes(workspace)
            }

    private fun List<R2dbcTypeEntity>.toDomainWithAttributes(workspace: UUID): Flux<Type> {
        if (isEmpty()) return Flux.empty()
        val typeIds = map { it.id }.distinct()
        return fetchAndGroupAttributes(workspace, typeIds).map { attributesMap ->
            map { r2dbcType ->
                val relatedAttributes = attributesMap[r2dbcType.id] ?: emptyList()
                r2dbcType.toDomain(relatedAttributes)
            }
        }.switchIfEmpty(Mono.just(emptyList()))
            .flatMapMany { Flux.fromIterable(it) }
    }
    private fun fetchAndGroupAttributes(workspace: UUID, typeIds: List<UUID>): Mono<Map<UUID, List<Attribute>>> = if(typeIds.isEmpty()) Mono.just(emptyMap())
    else query(
        where("workspace").`is`(workspace).
        and("type").`in`(typeIds)
    ).let {
        template.select(it, R2dbcAttributeEntity::class.java)
    }.collectList().map { r2dbcAttributes ->
        r2dbcAttributes.groupBy(R2dbcAttributeEntity::type) { attributeEntity ->
            attributeEntity.toDomain()
        }
    }

    private fun R2dbcTypeEntity.toDomain(relatedAttributes: List<Attribute>): Type = Type(
        id = name,
        version = version,
        effectDateTime = effectDateTime,
        expireDateTime = expireDateTime,
        description = description,
        primitive = primitive,
        attributes = relatedAttributes,
        parent = parent,
        x = x.unsigned(),
        y = y.unsigned(),
        width = width.unsigned(),
        height = height.unsigned()
    )
    private fun R2dbcAttributeEntity.toDomain(): Attribute = Attribute (
        name=name,
        type=objectMapper.readValue(attributeType.asArray(), AttributeTypeDefinition::class.java) ?: throw MissingFieldException("attributeType"),
        order=order,
        description=description,
        nullable=nullable,
        inherited=false
    )

    companion object {
        private val FIND_TYPE_WITH_PREV_AND_NEXT = """
            SELECT t.*,
                   prev.version AS prev,
                   next.version AS next
            FROM public.type t
            LEFT JOIN public.type prev ON prev.workspace = t.workspace
                                       AND prev.name = t.name
                                       AND prev.expire_at = t.effective_at
                                       AND prev.last=true
            LEFT JOIN public.type next ON next.workspace = t.workspace
                                       AND next.name = t.name
                                       AND next.effective_at = t.expire_at
                                       AND next.last=true
            WHERE t.workspace = :workspace AND t.last=true
        """.trimIndent()
        private val FIND_TYPE_WITH_PREV_AND_NEXT_BY_RANGE =
            "$FIND_TYPE_WITH_PREV_AND_NEXT AND t.effective_at <= :expireDateTime AND t.expire_at >= :effectDateTime"
        fun Short.unsigned(): UShort = (this + 32768).toUShort()
    }
}