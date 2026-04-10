package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.DocumentPatch
import dev.sayaya.handbook.usecase.DocumentService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

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
