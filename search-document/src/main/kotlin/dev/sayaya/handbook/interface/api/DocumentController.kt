package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.Search
import dev.sayaya.handbook.usecase.DocumentService
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

@RestController
class DocumentController(private val svc: DocumentService) {
    @GetMapping("/workspace/{workspace}/documents", produces = ["application/vnd.sayaya.handbook.v1+json"])
    fun search(@PathVariable workspace: UUID, query: Search): Mono<Page<Document>> = svc.search(workspace, query)
    @GetMapping("/workspace/{workspace}/{type}/{serial}", produces = ["application/vnd.sayaya.handbook.v1+json"])
    fun find(@PathVariable workspace: UUID,
             @PathVariable type: String,
             @PathVariable serial: String,
             @RequestParam(required = false) date: String?): Mono<Map<String, *>> = svc.find(workspace, type, serial, toInstant(date))

    private fun toInstant(dateString: String?): Instant? {
        if (dateString.isNullOrBlank()) return null
        for ((_, parser) in PARSERS) {
            try {
                return parser(dateString)
            } catch (_: DateTimeParseException) { }
        }
        val supportedFormatsMessage = PARSERS.joinToString(", ") { "'${it.first.substringBefore(" ")}'" } // 예: 'YYYY-MM-DDTHH:mm:ssZ', 'YYYY-MM-DD', 'yyyyMMdd', ...
        throw IllegalArgumentException("Invalid date format: '$dateString'. Please use one of the supported formats: $supportedFormatsMessage.")
    }


    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): Mono<String> {
        ex.printStackTrace()
        return Mono.justOrEmpty(ex.localizedMessage)
    }
    companion object {
        val PARSERS: List<Pair<String, (String) -> Instant>> = listOf(
            "YYYY-MM-DDTHH:mm:ssZ (ISO-8601 DateTime)" to Instant::parse ,
            "YYYY-MM-DD (ISO-8601 Date)" to { dateString -> LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay(ZoneOffset.UTC).toInstant() },
            "yyyyMMdd" to { dateString ->  LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMdd")).atStartOfDay(ZoneOffset.UTC).toInstant() },
            "yyyy.MM.dd" to { dateString -> LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy.MM.dd")).atStartOfDay(ZoneOffset.UTC).toInstant() },
            "yyyy-MM-dd HH:mm:ss" to { dateString -> LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toInstant(ZoneOffset.UTC) }
        )
    }
}