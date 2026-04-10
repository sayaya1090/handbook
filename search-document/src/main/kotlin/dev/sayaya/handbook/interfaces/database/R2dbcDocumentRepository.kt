package dev.sayaya.handbook.interfaces.database

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.Search
import dev.sayaya.handbook.usecase.DocumentRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

class R2dbcDocumentRepository(
    private val template: R2dbcEntityTemplate,
    private val objectMapper: ObjectMapper,
) : DocumentRepository {

    override fun search(workspace: UUID, param: Search): Mono<Page<Document>> {
        val filters = param.filters + ("workspace" to workspace)
        val pageable = createPageRequest(param)
        val criteria = buildCriteria(filters)
        val query = Query.query(criteria).with(pageable).columns("*, count(*) OVER() as count")
        return template.select(R2dbcDocumentEntity::class.java)
            .matching(query)
            .all()
            .collectList()
            .map { list ->
                if (list.isEmpty()) PageImpl(emptyList(), pageable, 0)
                else PageImpl(list.map { toDomain(it) }, pageable, list.first().count)
            }
    }

    override fun find(workspace: UUID, type: String, serial: String, date: Instant): Mono<Document> {
        val criteria = where("workspace").`is`(workspace)
            .and("type").`is`(type)
            .and("serial").`is`(serial)
            .and("effect_date_time").lessThanOrEquals(date)
            .and("expire_date_time").greaterThan(date)
        return template.select(R2dbcDocumentEntity::class.java)
            .matching(Query.query(criteria))
            .one()
            .map { toDomain(it) }
    }

    private fun createPageRequest(param: Search): PageRequest {
        val sortBy = param.sortBy?.let(::property) ?: "serial"
        val sortOrder = param.asc?.let {
            if (it) Sort.Order.asc(sortBy) else Sort.Order.desc(sortBy)
        } ?: Sort.Order.asc(sortBy)
        return PageRequest.of(param.page, param.limit, Sort.by(sortOrder))
    }

    private fun buildCriteria(filters: List<Pair<String, Any?>>): Criteria {
        if (filters.isEmpty()) return Criteria.empty()
        return filters.map { (key, value) -> predicate(key, value) }.reduce(Criteria::and)
    }

    private fun predicate(key: String, value: Any?): Criteria = when (key) {
        "workspace" -> if (value is UUID) where("workspace").`is`(value) else Criteria.empty()
        "date" -> when (value) {
            is String -> try {
                val date = value.toLong().let(Instant::ofEpochMilli)
                where("effect_date_time").lessThanOrEquals(date).and("expire_date_time").greaterThan(date)
            } catch (_: NumberFormatException) { Criteria.empty() }
            is Long -> {
                val date = Instant.ofEpochMilli(value)
                where("effect_date_time").lessThanOrEquals(date).and("expire_date_time").greaterThan(date)
            }
            else -> Criteria.empty()
        }
        else -> {
            val prop = property(key)
            when {
                prop == null -> Criteria.empty()
                value != null -> where(prop).`is`(value).ignoreCase(true)
                else -> where(prop).isNull
            }
        }
    }

    private fun property(name: String): String? = when (name) {
        "serial" -> "serial"
        "type" -> "type"
        else -> null
    }

    private fun toDomain(entity: R2dbcDocumentEntity): Document {
        val dataMap: Map<String, String?> = objectMapper.readValue(entity.data)
        return Document(
            id = entity.id,
            type = entity.type,
            serial = entity.serial,
            effectDateTime = entity.effectDateTime,
            expireDateTime = entity.expireDateTime,
            createDateTime = entity.createDateTime,
            creator = entity.creator,
            data = dataMap,
        )
    }
}
