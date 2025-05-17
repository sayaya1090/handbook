package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.Search
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@RestController
class DocumentController {
    @GetMapping("/workspace/{workspace}/documents", produces = ["application/vnd.sayaya.handbook.v1+json"])
    fun find(@PathVariable workspace: UUID, query: Search): Flux<Document> = TODO()

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): Mono<String> {
        ex.printStackTrace()
        return Mono.justOrEmpty(ex.localizedMessage)
    }
}