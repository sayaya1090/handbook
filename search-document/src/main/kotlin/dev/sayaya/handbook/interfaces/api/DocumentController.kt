package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.DiffResult
import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.Search
import dev.sayaya.handbook.usecase.DocumentSearchService
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

/**
 * 문서 조회 REST 컨트롤러 (읽기 전용 CQRS).
 *
 * **책임:** 문서 검색, 특정 시점 조회, 두 시점 간 diff 제공.
 *
 * **의존관계:**
 * - [DocumentSearchService] — 조회/diff 비즈니스 로직
 */
@RestController
class DocumentController(private val svc: DocumentSearchService) {
    @GetMapping("/workspace/{workspace}/documents", produces = ["application/vnd.sayaya.handbook.v1+json"])
    fun search(@PathVariable workspace: UUID, query: Search): Mono<Page<Document>> = svc.search(workspace, query)

    @GetMapping("/workspace/{workspace}/{type}/{serial}", produces = ["application/vnd.sayaya.handbook.v1+json"])
    fun find(
        @PathVariable workspace: UUID,
        @PathVariable type: String,
        @PathVariable serial: String,
        @RequestParam(required = false) date: String?,
    ): Mono<Document> = svc.find(workspace, type, serial, toInstant(date))

    @GetMapping("/workspace/{workspace}/documents/search", produces = ["application/vnd.sayaya.handbook.v1+json"])
    fun fullTextSearch(
        @PathVariable workspace: UUID,
        @RequestParam q: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") limit: Int,
    ): Mono<Page<Document>> {
        if (q.length > MAX_QUERY_LENGTH) {
            return Mono.error(ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Search query exceeds maximum length of $MAX_QUERY_LENGTH characters"
            ))
        }
        return svc.fullTextSearch(workspace, q, page, limit)
    }

    @GetMapping("/workspace/{workspace}/{type}/{serial}/history", produces = ["application/vnd.sayaya.handbook.v1+json"])
    fun history(
        @PathVariable workspace: UUID,
        @PathVariable type: String,
        @PathVariable serial: String,
    ): Flux<Document> = svc.findHistory(workspace, type, serial)

    @GetMapping("/workspace/{workspace}/{type}/{serial}/diff", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun diff(
        @PathVariable workspace: UUID,
        @PathVariable type: String,
        @PathVariable serial: String,
        @RequestParam date1: String,
        @RequestParam date2: String,
    ): Mono<DiffResult> = svc.diff(workspace, type, serial, toInstant(date1)!!, toInstant(date2)!!)

    private fun toInstant(dateString: String?): Instant? {
        if (dateString.isNullOrBlank()) return null
        for ((_, parser) in PARSERS) {
            try { return parser(dateString) } catch (_: DateTimeParseException) { }
        }
        val formats = PARSERS.joinToString(", ") { "'${it.first}'" }
        throw IllegalArgumentException("Invalid date format: '$dateString'. Supported: $formats")
    }

    companion object {
        const val MAX_QUERY_LENGTH = 1000

        val PARSERS: List<Pair<String, (String) -> Instant>> = listOf(
            "ISO-8601 DateTime" to Instant::parse,
            "ISO-8601 Date" to { s -> LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay(ZoneOffset.UTC).toInstant() },
            "yyyyMMdd" to { s -> LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyyMMdd")).atStartOfDay(ZoneOffset.UTC).toInstant() },
            "yyyy.MM.dd" to { s -> LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy.MM.dd")).atStartOfDay(ZoneOffset.UTC).toInstant() },
            "yyyy-MM-dd HH:mm:ss" to { s -> LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toInstant(ZoneOffset.UTC) },
        )
    }
}
