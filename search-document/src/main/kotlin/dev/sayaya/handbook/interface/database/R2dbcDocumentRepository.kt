package dev.sayaya.handbook.`interface`.database

import com.fasterxml.jackson.databind.ObjectMapper
import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.Search
import dev.sayaya.handbook.usecase.DocumentRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.sql.SqlIdentifier
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.time.Instant
import java.util.UUID

@Repository
class R2dbcDocumentRepository(
    private val template: R2dbcEntityTemplate,
    private val objectMapper: ObjectMapper
): DocumentRepository, R2dbcSearchable<R2dbcDocumentEntity, Document> {
    @Transactional(readOnly = true)
    override fun search(workspace: UUID, param: Search): Mono<Page<Document>> = param.copy(
        filters = param.filters + ("last" to true) + ("workspace" to workspace)
    ).let(::search).flatMap { page ->
        if (page.content.isEmpty()) Mono.empty()
        else page.toMono()
    }
    override fun search(param: Search): Mono<Page<Document>> {
        val pageable = createPageRequest(param)
        return template.search(SqlIdentifier.unquoted("document_with_validation"), param.filters, R2dbcDocumentEntity::class.java, pageable)
            .map { it.map { toDomain(it) } }
    }
    private fun createPageRequest(param: Search): PageRequest {
        val sortBy = param.sortBy?.let(::property) ?: "serial"
        val sortOrder = param.asc?.let { if (it) Sort.Order.asc(sortBy) else Sort.Order.desc(sortBy) } ?: Sort.Order.desc(sortBy)
        return PageRequest.of(param.page, param.limit, Sort.by(sortOrder))
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
            when {
                property==null  -> Criteria.empty()
                value!=null     -> where(property).`is`(value).ignoreCase(true)
                else            -> where(property).isNull
            }
        }
    }
    private fun property(name: String): String? = when(name) {
        "serial" -> "serial"
        "type" -> "type"
        else -> null
    }
    private fun toDomain(entity: R2dbcDocumentEntity): Document = Document (
        id = entity.id,
        type = entity.type,
        serial = entity.serial,
        effectDateTime = entity.effectDateTime,
        expireDateTime = entity.expireDateTime,
        createDateTime = entity.createDateTime,
        creator = entity.creatorUserName,
        data = objectMapper.readValue(entity.data.asArray(), Map::class.java) as Map<String, String?>,
    )
}
