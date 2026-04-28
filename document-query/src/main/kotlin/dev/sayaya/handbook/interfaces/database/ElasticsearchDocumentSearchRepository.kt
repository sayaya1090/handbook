package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.Search
import dev.sayaya.handbook.usecase.DocumentSearchRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations
import org.springframework.data.elasticsearch.core.query.Criteria
import org.springframework.data.elasticsearch.core.query.CriteriaQuery
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

/**
 * 문서 읽기 전용 Elasticsearch 어댑터 (CQRS query-side).
 *
 * **책임:** Elasticsearch 를 사용하여 문서 검색 및 전문 검색을 수행한다.
 */
@Repository
class ElasticsearchDocumentSearchRepository(
    private val operations: ReactiveElasticsearchOperations
) : DocumentSearchRepository {

    override fun search(workspace: UUID, param: Search): Mono<Page<Document>> {
        val pageable = createPageRequest(param)
        var criteria = Criteria("workspace").`is`(workspace)
        
        param.filters.forEach { (key, value) ->
            if (value != null) {
                when (key) {
                    "type" -> criteria = criteria.and(Criteria("type").`is`(value))
                    "serial" -> criteria = criteria.and(Criteria("serial").`is`(value))
                }
            }
        }

        // Use explicit variable type to help Kotlin compiler with generic inference
        val query: Query = CriteriaQuery(criteria).setPageable(pageable)
        
        return operations.searchForPage(query, ElasticsearchDocumentEntity::class.java)
            .map { searchPage ->
                PageImpl(
                    searchPage.content.map { it.content.toDomain() },
                    pageable,
                    searchPage.totalElements
                )
            }
    }

    override fun find(workspace: UUID, type: String, serial: String, date: Instant): Mono<Document> {
        val criteria = Criteria("workspace").`is`(workspace)
            .and(Criteria("type").`is`(type))
            .and(Criteria("serial").`is`(serial))
            .and(Criteria("effectDateTime").lessThanEqual(date))
            .and(Criteria("expireDateTime").greaterThan(date))
            
        val query: Query = CriteriaQuery(criteria)
        return operations.search(query, ElasticsearchDocumentEntity::class.java)
            .next()
            .map { it.content.toDomain() }
    }

    override fun findHistory(workspace: UUID, type: String, serial: String): Flux<Document> {
        val criteria = Criteria("workspace").`is`(workspace)
            .and(Criteria("type").`is`(type))
            .and(Criteria("serial").`is`(serial))
            
        val query: Query = CriteriaQuery(criteria).addSort(Sort.by(Sort.Direction.DESC, "effectDateTime"))
        return operations.search(query, ElasticsearchDocumentEntity::class.java)
            .map { it.content.toDomain() }
    }

    override fun fullTextSearch(workspace: UUID, query: String, page: Int, limit: Int): Mono<Page<Document>> {
        val pageable = PageRequest.of(page, limit)
        
        // Delegating to Java helper to bypass Kotlin type inference issues with complex ES Query DSL
        val nativeQuery = ElasticsearchQueryBuilder.buildFullTextNativeQuery(workspace, query, pageable)

        return operations.searchForPage(nativeQuery, ElasticsearchDocumentEntity::class.java)
            .map { searchPage ->
                PageImpl(
                    searchPage.content.map { it.content.toDomain() },
                    pageable,
                    searchPage.totalElements
                )
            }
    }

    private fun createPageRequest(param: Search): PageRequest {
        val sortBy = param.sortBy ?: "serial"
        val direction = if (param.asc == true) Sort.Direction.ASC else Sort.Direction.DESC
        return PageRequest.of(param.page, param.limit, Sort.by(direction, sortBy))
    }
}
