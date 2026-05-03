package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.Search
import dev.sayaya.handbook.usecase.DocumentSearchRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import java.time.Instant
import java.util.*

/**
 * 문서 읽기 전용 R2DBC 어댑터 (CQRS query-side).
 *
 * **책임:** R2DBC를 사용하여 문서 검색 및 특정 시점 조회를 수행한다.
 * document-command 모듈의 R2DBC 어댑터와 네임스페이스 충돌을 방지하기 위해 "Search" 접두사를 사용한다.
 *
 * **의존관계:**
 * - [R2dbcEntityTemplate] — Spring Data R2DBC 쿼리 실행
 * - [ObjectMapper] — JSON data 필드 역직렬화
 */
class R2dbcDocumentSearchRepository(
    private val template: R2dbcEntityTemplate,
    private val objectMapper: ObjectMapper,
) : DocumentSearchRepository {

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

    override fun fullTextSearch(workspace: UUID, query: String, page: Int, limit: Int): Mono<Page<Document>> {
        val pageable = PageRequest.of(page, limit)
        val likePattern = "%${query.replace("%", "\\%").replace("_", "\\_")}%"
        return template.databaseClient
            .sql("""
                SELECT *, count(*) OVER() as count FROM documents
                WHERE workspace = :workspace AND data::text ILIKE :pattern
                ORDER BY serial ASC
                LIMIT :limit OFFSET :offset
            """.trimIndent())
            .bind("workspace", workspace)
            .bind("pattern", likePattern)
            .bind("limit", limit)
            .bind("offset", page * limit)
            .map { row, _ ->
                R2dbcDocumentEntity(
                    workspace = row.get("workspace", UUID::class.java)!!,
                    id = row.get("id", UUID::class.java)!!,
                    type = row.get("type", String::class.java)!!,
                    serial = row.get("serial", String::class.java)!!,
                    effectDateTime = row.get("effect_date_time", java.time.Instant::class.java)!!,
                    expireDateTime = row.get("expire_date_time", java.time.Instant::class.java)!!,
                    createDateTime = row.get("create_date_time", java.time.Instant::class.java)!!,
                    creator = row.get("creator", String::class.java)!!,
                    data = row.get("data", String::class.java)!!,
                    count = (row.get("count", Long::class.javaObjectType) ?: 0L),
                )
            }
            .all()
            .collectList()
            .map { list ->
                if (list.isEmpty()) PageImpl(emptyList(), pageable, 0)
                else PageImpl(list.map { toDomain(it) }, pageable, list.first().count)
            }
    }

    override fun findHistory(workspace: UUID, type: String, serial: String): Flux<Document> {
        val criteria = where("workspace").`is`(workspace)
            .and("type").`is`(type)
            .and("serial").`is`(serial)
        return template.select(R2dbcDocumentEntity::class.java)
            .matching(Query.query(criteria).sort(Sort.by(Sort.Order.desc("effect_date_time"))))
            .all()
            .map { toDomain(it) }
    }

    internal fun createPageRequest(param: Search): PageRequest {
        val sortBy = param.sortBy?.let(::property) ?: "serial"
        val sortOrder = param.asc?.let {
            if (it) Sort.Order.asc(sortBy) else Sort.Order.desc(sortBy)
        } ?: Sort.Order.asc(sortBy)
        return PageRequest.of(param.page, param.limit, Sort.by(sortOrder))
    }

    internal fun buildCriteria(filters: List<Pair<String, Any?>>): Criteria {
        if (filters.isEmpty()) return Criteria.empty()
        return filters.map { (key, value) -> predicate(key, value) }.reduce(Criteria::and)
    }

    internal fun predicate(key: String, value: Any?): Criteria = when (key) {
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

    internal fun property(name: String): String? = when (name) {
        "serial" -> "serial"
        "type" -> "type"
        else -> null
    }

    internal fun toDomain(entity: R2dbcDocumentEntity): Document {
        val dataMap: Map<String, String?> = objectMapper.readValue(entity.data)
        val map = java.lang.reflect.Proxy.newProxyInstance(
            jsinterop.base.JsPropertyMap::class.java.classLoader,
            arrayOf(jsinterop.base.JsPropertyMap::class.java, Map::class.java)
        ) { _, method, args ->
            if (method.declaringClass == Map::class.java) {
                method.invoke(dataMap, *(args ?: emptyArray()))
            } else when (method.name) {
                "get" -> dataMap[args[0] as String]
                "set" -> null
                "forEach" -> null
                else -> null
            }
        } as jsinterop.base.JsPropertyMap<String>
        return Document.create(
            entity.id.toString(),
            entity.type,
            entity.serial,
            entity.effectDateTime.toEpochMilli().toDouble(),
            entity.expireDateTime.toEpochMilli().toDouble(),
            entity.createDateTime.toEpochMilli().toDouble(),
            entity.creator,
            map
        ).status("PUBLISHED")
    }
}

