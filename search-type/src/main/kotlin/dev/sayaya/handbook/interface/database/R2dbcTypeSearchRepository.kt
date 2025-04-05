package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.Search
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.usecase.TypeSearchRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.sql.SqlIdentifier
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@Repository
class R2dbcTypeSearchRepository(private val template: R2dbcEntityTemplate, private val attributeRepo: R2dbcAttributeRepository): TypeSearchRepository, R2dbcSearchable<R2dbcTypeEntity, R2dbcTypeEntity> {
    @Transactional(readOnly = true)
    override fun search(workspace: UUID, param: Search): Mono<Page<Type>> = param.copy(
        filters = param.filters + ("last" to true) + ("workspace" to workspace)
    ).let(::search).flatMap { page ->
        if (page.content.isEmpty()) Mono.empty()
        else mapToTypes(workspace, page)
    }
    override fun search(param: Search): Mono<Page<R2dbcTypeEntity>> {
        val pageable = createPageRequest(param)
        return template.search(SqlIdentifier.unquoted("type"), param.filters, R2dbcTypeEntity::class.java, pageable)
    }
    private fun createPageRequest(param: Search): PageRequest {
        val sortBy = param.sortBy?.let(::property) ?: "created_at"
        val sortOrder = param.asc?.let { if (it) Sort.Order.asc(sortBy) else Sort.Order.desc(sortBy) } ?: Sort.Order.desc(sortBy)
        return PageRequest.of(param.page, param.limit, Sort.by(sortOrder))
    }
    private fun mapToTypes(workspace: UUID, page: Page<R2dbcTypeEntity>): Mono<Page<Type>> = page.content.map { it.id }.let { typeIds ->
        attributeRepo.findAllByTypeIds(workspace, typeIds).map { attributesMap ->
            val types = page.content.map { typeEntity ->
                val attributes = attributesMap[typeEntity.id] ?: emptyList()
                toDomain(typeEntity, attributes)
            }
            PageImpl(types, page.pageable, page.totalElements)
        }
    }
    override fun R2dbcEntityTemplate.predicate(key: String, value: Any?): Criteria = when (key) {
        "workspace" -> if (value is UUID) where("workspace").`is`(value) else Criteria.empty()
        "last" -> if (value is Boolean) { where("last").`is`(value) } else Criteria.empty()
        "date" -> when (value) {
            is String -> try {
                val date = value.toLong().let(Instant::ofEpochMilli)
                where("effective_at").lessThanOrEquals(date).and("expire_at").greaterThan(date)
            } catch (e: NumberFormatException) {
                Criteria.empty()
            }
            is Long -> {
                val date = Instant.ofEpochMilli(value)
                where("effective_at").lessThanOrEquals(date).and("expire_at").greaterThan(date)
            } else -> Criteria.empty()
        } else -> {
            val property = property(key)
            if(property==null) Criteria.empty()
            else where(property).`is`(value).ignoreCase(true)
        }
    }
    private fun property(name: String): String? = when(name) {
        "name" -> "name"
        else -> null
    }
    private fun toDomain(entity: R2dbcTypeEntity, attributes: List<Attribute>): Type = Type (
        id = entity.name,
        parent = entity.parent,
        version = entity.version,
        effectDateTime = entity.effectDateTime,
        expireDateTime = entity.expireDateTime,
        description = entity.description,
        primitive = entity.primitive,
        attributes = attributes
    )
}