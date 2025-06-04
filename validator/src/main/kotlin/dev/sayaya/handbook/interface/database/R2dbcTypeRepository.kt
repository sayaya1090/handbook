package dev.sayaya.handbook.`interface`.database

import com.fasterxml.jackson.databind.ObjectMapper
import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.AttributeTypeDefinition
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.`interface`.cache.TypeRangeCache
import dev.sayaya.handbook.usecase.TypeRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.UUID
import kotlin.IllegalStateException

@Repository
class R2dbcTypeRepository(
    private val cache: TypeRangeCache,
    private val template: R2dbcEntityTemplate,
    private val objectMapper: ObjectMapper
) : TypeRepository {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
    override fun cache(workspace: UUID, type: Type): Mono<Void> = cache.saveType(workspace, type)

    override fun find(workspace: UUID, id: String, effectDateTime: Instant, expireDateTime: Instant): Mono<Type> {
        return cache.findTypesByTimeRange(workspace, id, effectDateTime, expireDateTime).switchIfEmpty(
            findTypeFromDB(workspace, id, effectDateTime, expireDateTime).delayUntil {
                cache(workspace, it)
            }
        ).doOnNext { log.info("Found type: workspace={}, id={}, version={}", workspace, id, it.version) }
    }
    private fun findTypeFromDB(workspace: UUID, id: String, effectDateTime: Instant, expireDateTime: Instant): Mono<Type> = template.databaseClient.sql(FIND_TYPE_WITH_PREV_AND_NEXT_BY_RANGE).bind("workspace", workspace)
        .bind("id", id)
        .bind("effectDateTime", effectDateTime)
        .bind("expireDateTime", expireDateTime)
        .map { row -> row.toEntity() }
        .one()
        .flatMap { r2dbcType ->
            r2dbcType.toDomainWithAttributes(workspace)
        }
    private fun R2dbcTypeEntity.toDomainWithAttributes(workspace: UUID): Mono<Type> = query (
            where("workspace").`is`(workspace).
            and("type").`is`(id)
        ).let {
            template.select(it, R2dbcAttributeEntity::class.java)
        }.map { it.toDomain() }
        .collectList().map { attributes ->
            toDomain(attributes)
        }

    private fun R2dbcAttributeEntity.toDomain(): Attribute = Attribute (
        name=name,
        type=objectMapper.readValue(attributeType.asArray(), AttributeTypeDefinition::class.java) ?: throw IllegalStateException("attributeType"),
        order=order,
        description=description,
        nullable=nullable,
        inherited=false
    )
    companion object {
        private fun io.r2dbc.spi.Readable.toEntity(): R2dbcTypeEntity = R2dbcTypeEntity(
            workspace = get("workspace", UUID::class.java) ?: throw IllegalStateException("workspace"),
            id = get("id", UUID::class.java) ?: throw IllegalStateException("id"),
            name = get("name", String::class.java) ?: throw IllegalStateException("name"),
            version = get("version", String::class.java) ?: throw IllegalStateException("version"),
            parent = get("parent", String::class.java),
            effectDateTime = get("effective_at", Instant::class.java) ?: throw IllegalStateException("effective_at"),
            expireDateTime = get("expire_at", Instant::class.java) ?: throw IllegalStateException("expire_at"),
            description = get("description", String::class.java) ?: throw IllegalStateException("description"),
            primitive = get("primitive", Boolean::class.java) ?: false,
            createDateTime = get("created_at", Instant::class.java) ?: throw IllegalStateException("created_at"),
            createBy = get("created_by", UUID::class.java) ?: throw IllegalStateException("created_by"),
            x = get("x", Short::class.java) ?: 0,
            y = get("y", Short::class.java) ?: 0,
            width = get("width", Short::class.java) ?: 0,
            height = get("height", Short::class.java) ?: 0
        )
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
        private val FIND_TYPE_WITH_PREV_AND_NEXT_BY_RANGE = """
            SELECT t.*
            FROM public.type t
            WHERE t.workspace = :workspace AND t.name = :id
            AND t.effective_at < :expireDateTime AND t.expire_at > :effectDateTime
            AND t.last=true
        """.trimIndent()
        fun Short.unsigned(): UShort = (this + 32768).toUShort()
    }
}
