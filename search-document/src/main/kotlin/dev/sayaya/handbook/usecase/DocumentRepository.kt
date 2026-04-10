package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.Search
import org.springframework.data.domain.Page
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

/**
 * 문서 읽기 전용 조회 포트 (CQRS query-side).
 *
 * **책임:** 문서 검색 및 특정 시점 조회를 정의한다.
 * persist-document 모듈의 CUD 포트와 네임스페이스 충돌을 방지하기 위해 "Search" 접두사를 사용한다.
 */
interface DocumentSearchRepository {
    fun search(workspace: UUID, param: Search): Mono<Page<Document>>
    fun find(workspace: UUID, type: String, serial: String, date: Instant): Mono<Document>
    fun findHistory(workspace: UUID, type: String, serial: String): Flux<Document>
    fun fullTextSearch(workspace: UUID, query: String, page: Int, limit: Int): Mono<Page<Document>>
}
