package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.DiffResult
import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.Search
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

/**
 * 문서 읽기 전용 비즈니스 로직 (CQRS query-side).
 *
 * **책임:** 문서 검색, 특정 시점 조회, 두 시점 간 diff 계산.
 * document-command 모듈의 CUD 서비스와 네임스페이스 충돌을 방지하기 위해 "Search" 접두사를 사용한다.
 *
 * **의존관계:**
 * - [DocumentSearchRepository] — R2DBC 조회 포트
 */
class DocumentSearchService(private val repo: DocumentSearchRepository) {
    fun search(workspace: UUID, param: Search): Mono<Page<Document>> = repo.search(workspace, param)
    fun find(workspace: UUID, type: String, serial: String, date: Instant?): Mono<Document> {
        val effectiveDate = date ?: Instant.now()
        return repo.find(workspace, type, serial, effectiveDate)
    }

    /**
     * 문서의 data JSONB 필드를 대상으로 전문 검색을 수행한다.
     * 검색어가 비어있으면 빈 페이지를 반환한다.
     */
    fun fullTextSearch(workspace: UUID, query: String, page: Int, limit: Int): Mono<Page<Document>> {
        if (query.isBlank()) return Mono.just(PageImpl(emptyList()))
        return repo.fullTextSearch(workspace, query, page, limit)
    }

    /**
     * 문서의 변경 이력(스냅샷 목록)을 조회한다.
     * effectDateTime 기준 내림차순으로 정렬된 문서 버전 목록을 반환한다.
     */
    fun findHistory(workspace: UUID, type: String, serial: String): Flux<Document> =
        repo.findHistory(workspace, type, serial)

    /**
     * 필터 조건에 맞는 문서를 모두 조회한다 (내보내기용, 페이지네이션 무시).
     * 내부적으로 limit을 최대값으로 설정하여 전체 결과를 조회한다.
     */
    fun findAllForExport(workspace: UUID, param: Search): Mono<List<Document>> {
        val exportParam = Search(page = 0, limit = Int.MAX_VALUE, sortBy = param.sortBy, asc = param.asc, filters = param.filters)
        return repo.search(workspace, exportParam).map { it.content }
    }

    /**
     * 문서의 두 시점 간 diff를 계산한다.
     * data 필드의 각 키를 비교하여 추가/삭제/변경을 감지한다.
     */
    fun diff(workspace: UUID, type: String, serial: String, date1: Instant, date2: Instant): Mono<DiffResult> {
        return Mono.zip(
            repo.find(workspace, type, serial, date1),
            repo.find(workspace, type, serial, date2),
        ).map { tuple ->
            val old = tuple.t1
            val new = tuple.t2
            val changes = mutableListOf<String>()
            val added = mutableListOf<String>()
            val removed = mutableListOf<String>()

            val om = tools.jackson.module.kotlin.jacksonObjectMapper()
            val oldJson = om.writeValueAsString(old.data() ?: emptyMap<String, Any>())
            val newJson = om.writeValueAsString(new.data() ?: emptyMap<String, Any>())
            val oldData: Map<String, Any?> = om.readValue(oldJson, object : tools.jackson.core.type.TypeReference<Map<String, Any?>>() {})
            val newData: Map<String, Any?> = om.readValue(newJson, object : tools.jackson.core.type.TypeReference<Map<String, Any?>>() {})

            (newData.keys - oldData.keys).forEach { added.add(it) }
            (oldData.keys - newData.keys).forEach { removed.add(it) }

            oldData.keys.intersect(newData.keys).forEach { key ->
                val oldVal = oldData[key]
                val newVal = newData[key]
                if (oldVal != newVal) {
                    changes.add("$key: ${oldVal ?: "(null)"} → ${newVal ?: "(null)"}")
                }
            }

            DiffResult(changes, added, removed)
        }
    }
}
