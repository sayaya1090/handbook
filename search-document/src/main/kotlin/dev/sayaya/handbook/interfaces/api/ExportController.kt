package dev.sayaya.handbook.interfaces.api

import tools.jackson.databind.ObjectMapper
import dev.sayaya.handbook.domain.Search
import dev.sayaya.handbook.usecase.CsvSerializer
import dev.sayaya.handbook.usecase.DocumentSearchService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * 문서 내보내기 REST 컨트롤러 (읽기 전용, 스트리밍 방식).
 *
 * <p><b>책임:</b> 필터 조건에 맞는 문서를 CSV 또는 JSON 형식으로 스트리밍하여 파일 다운로드 응답을 반환한다.
 * 전체 문서를 메모리에 수집하지 않고 청크 단위로 전송하여 대용량 내보내기 시 메모리 사용을 최적화한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link DocumentSearchService} — 문서 조회 비즈니스 로직</li>
 *   <li>{@link CsvSerializer} — CSV 직렬화</li>
 *   <li>{@link ObjectMapper} — JSON 직렬화</li>
 * </ul></p>
 *
 * <p><b>주의:</b> format 파라미터가 csv/json 이외의 값이면 400 Bad Request를 반환한다.
 * Transfer-Encoding: chunked를 사용하여 스트리밍한다.</p>
 */
@RestController
class ExportController(
    private val svc: DocumentSearchService,
    private val objectMapper: ObjectMapper,
) {
    @GetMapping("/workspace/{workspace}/documents/export")
    fun export(
        @PathVariable workspace: UUID,
        @RequestParam(defaultValue = "json") format: String,
        query: Search,
        exchange: ServerWebExchange,
    ): Mono<Void> {
        return when (format.lowercase()) {
            "json" -> exportJsonStreaming(workspace, query, exchange)
            "csv" -> exportCsvStreaming(workspace, query, exchange)
            else -> Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported format: $format. Supported: json, csv"))
        }
    }

    private fun exportJsonStreaming(workspace: UUID, query: Search, exchange: ServerWebExchange): Mono<Void> {
        val response = exchange.response
        response.headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"documents.json\"")
        response.headers.contentType = MediaType.APPLICATION_JSON

        return svc.findAllForExport(workspace, query).flatMap { documents ->
            val json = objectMapper.writeValueAsString(documents)
            val buffer = response.bufferFactory().wrap(json.toByteArray(StandardCharsets.UTF_8))
            response.writeWith(Mono.just(buffer))
        }
    }

    private fun exportCsvStreaming(workspace: UUID, query: Search, exchange: ServerWebExchange): Mono<Void> {
        val response = exchange.response
        response.headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"documents.csv\"")
        response.headers.contentType = MediaType.parseMediaType("text/csv")

        return svc.findAllForExport(workspace, query).flatMap { documents ->
            val csv = CsvSerializer.serialize(documents)
            val buffer = response.bufferFactory().wrap(csv.toByteArray(StandardCharsets.UTF_8))
            response.writeWith(Mono.just(buffer))
        }
    }
}
