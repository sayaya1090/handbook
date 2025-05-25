package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.Search
import dev.sayaya.handbook.usecase.DocumentService
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*

@RestController
class DocumentController(private val svc: DocumentService) {
    @GetMapping("/workspace/{workspace}/documents", produces = ["application/vnd.sayaya.handbook.v1+json"])
    fun find(@PathVariable workspace: UUID, query: Search): Mono<Page<Document>> = svc.search(workspace, query)

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): Mono<String> {
        ex.printStackTrace()
        return Mono.justOrEmpty(ex.localizedMessage)
    }
}