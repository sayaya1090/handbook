package dev.sayaya.handbook.`interface`.database

import dev.sayaya.domain.Search
import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.usecase.TypeSearchRepository
import dev.sayaya.`interface`.database.R2dbcSearchable
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

@Repository
class R2dbcTypeSearchRepository(private val template: R2dbcEntityTemplate, private val attributeRepo: R2dbcAttributeRepository): TypeSearchRepository, R2dbcSearchable<R2dbcTypeEntity, Type> {
    @Transactional(readOnly = true)
    override fun search(param: Search): Mono<Page<Type>> {
        val pageable = createPageRequest(param)
        val filters = prepareFilters(param.filters)
        return template.search(SqlIdentifier.unquoted("type"), filters, R2dbcTypeEntity::class.java, pageable).flatMap { page ->
            if (page.content.isEmpty()) Mono.empty()
            else mapToTypes(page)
        }
    }
    private fun createPageRequest(param: Search): PageRequest {
        val sortBy = param.sortBy?.let(::property) ?: "created_at"
        val sortOrder = param.asc?.let { if (it) Sort.Order.asc(sortBy) else Sort.Order.desc(sortBy) } ?: Sort.Order.desc(sortBy)
        return PageRequest.of(param.page, param.limit, Sort.by(sortOrder))
    }
    private fun prepareFilters(filters: List<Pair<String, String>>): List<Pair<String, String>> {
        val augmentedFilters = if (filters.none { (key, _) -> key == "date" }) filters + ("date" to Instant.now().toEpochMilli().toString())
        else filters
        return augmentedFilters + ("last" to "true")
    }
    private fun mapToTypes(page: Page<R2dbcTypeEntity>): Mono<Page<Type>> = page.content.map { it.id }.let { typeIds ->
        attributeRepo.findAllByTypeIds(typeIds).map { attributesMap ->
            val types = page.content.map { typeEntity ->
                val attributes = attributesMap[typeEntity.id] ?: emptyList()
                toDomain(typeEntity, attributes)
            }
            PageImpl(types, page.pageable, page.totalElements)
        }
    }
    override fun R2dbcEntityTemplate.predicate(key: String, value: String): Criteria = when (key) {
        "last" -> where("last").`is`(value.toBooleanStrict()).ignoreCase(true)
        "date" -> where(value).between("effective_at", "expire_at")
        else -> {
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