package dev.sayaya.handbook.interfaces.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.usecase.DocumentService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * 문서 일괄 임포트/익스포트 REST 컨트롤러.
 *
 * **책임:** JSON 형식으로 문서를 일괄 임포트(POST)하고, 익스포트(GET)한다.
 * 임포트 시 각 문서를 DocumentService.save()로 저장하여 이벤트도 발행된다.
 *
 * **의존관계:**
 * - [DocumentService] — 문서 저장/이벤트 발행
 * - [ObjectMapper] — JSON 직렬화/역직렬화
 *
 * **주의:** CSV 지원은 향후 추가 예정. 현재는 JSON만 지원한다.
 * 대량 임포트 시 트랜잭션 크기에 주의해야 한다.
 */
@RestController
@RequestMapping("/workspace/{workspace}/documents")
class ImportExportController(
    private val documentService: DocumentService,
    private val objectMapper: ObjectMapper,
) {
    @PostMapping(
        "/import",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun import(
        @PathVariable workspace: UUID,
        @RequestBody documents: List<Document>,
    ): Flux<Document> = documentService.save(workspace, documents)

    @GetMapping(
        "/export",
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun export(
        @PathVariable workspace: UUID,
        @RequestParam(required = false) type: String?,
    ): Mono<ResponseEntity<String>> {
        // search-document 서비스를 직접 호출하지 않고,
        // 로컬 저장소에서 전체 문서를 조회하여 JSON으로 반환
        return documentService.findAll(workspace, type)
            .collectList()
            .map { docs ->
                val json = objectMapper.writeValueAsString(docs)
                ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"documents-export.json\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
            }
    }
}
