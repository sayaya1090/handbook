package dev.sayaya.handbook.`interface`.database

import com.fasterxml.jackson.databind.ObjectMapper
import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.AttributeType
import dev.sayaya.handbook.domain.AttributeTypeDefinition
import dev.sayaya.handbook.domain.Layout
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
    override fun findAll(workspace: UUID): Flux<Layout> = template.databaseClient.sql(FIND_LAYOUT_SQL)
        .bind("workspace", workspace).map { row ->
            val effectDateTime = row.get("effective_at", Instant::class.java)!!
            val expireDateTime = row.get("expire_at", Instant::class.java)!!
            effectDateTime to expireDateTime
        }.all().collectList().flatMapIterable { list ->
            val set = TreeSet<Instant>()
            list.forEach { (effectDateTime, expireDateTime) ->
                set.add(effectDateTime)
                set.add(expireDateTime)
            }
            if (set.size < 2) emptyList()
            else set.toList().zipWithNext().map { (start, end) ->
                Layout(
                    workspace = workspace,
                    effectDateTime = start,
                    expireDateTime = end
                )
            }
        }

    override fun findByRange(workspace: UUID, effectDateTime: Instant, expireDateTime: Instant): Flux<Type> = query(
        where("workspace").`is`(workspace)
            .and("effective_at").lessThan(expireDateTime)
            .and("expire_at").greaterThan(effectDateTime)
            .and("last").`is`(true)
    ).let { template.select(it, R2dbcTypeEntity::class.java) }
        .collectList()
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
        private val FIND_LAYOUT_SQL = """
            SELECT t.effective_at, t.expire_at FROM type t WHERE t.workspace = :workspace AND t.last=true
        """.trimIndent()
        fun Short.unsigned(): UShort = (this + 32768).toUShort()
    }
}