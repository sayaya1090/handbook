package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.DocumentPatch
import dev.sayaya.handbook.usecase.DocumentService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * 문서 CUD REST 컨트롤러.
 *
 * **책임:** 워크스페이스 단위로 문서의 저장(PUT), 부분 수정(PATCH), 삭제(DELETE) 요청을 처리한다.
 * 커스텀 미디어 타입 `application/vnd.sayaya.handbook.v1+json`을 사용한다.
 *
 * **의존관계:**
 * - [DocumentService] — 문서 비즈니스 로직 (저장/패치/삭제 + 이벤트 발행)
 *
 * **주의:** 모든 엔드포인트는 워크스페이스 UUID를 경로 변수로 받으며,
 * PATCH 시 rev 불일치가 발생하면 409 Conflict를 반환한다.
 */
@RestController
@RequestMapping("/workspace/{workspace}/documents")
class DocumentController(private val documentService: DocumentService) {

    @PutMapping(
        consumes = ["application/vnd.sayaya.handbook.v1+json"],
        produces = ["application/vnd.sayaya.handbook.v1+json"],
    )
    @ResponseStatus(HttpStatus.OK)
    fun save(
        @PathVariable workspace: UUID,
        @RequestBody documents: List<Document>,
    ): Flux<Document> = documentService.save(workspace, documents)

    @PatchMapping(
        consumes = ["application/vnd.sayaya.handbook.v1+json"],
        produces = ["application/vnd.sayaya.handbook.v1+json"],
    )
    @ResponseStatus(HttpStatus.OK)
    fun patch(
        @PathVariable workspace: UUID,
        @RequestBody patches: List<DocumentPatch>,
    ): Flux<Document> = documentService.patch(workspace, patches)

    @DeleteMapping(consumes = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable workspace: UUID,
        @RequestBody documents: List<Document>,
    ): Mono<Void> = documentService.delete(workspace, documents)
}
