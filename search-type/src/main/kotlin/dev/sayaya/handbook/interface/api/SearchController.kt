package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.domain.Search
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.usecase.TypeSearchService
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*

@RestController
class SearchController(private val svc: TypeSearchService) {
    @GetMapping("/workspace/{workspace}/types", produces = ["application/vnd.sayaya.handbook.v1+json"])
    fun findWorklists(@PathVariable workspace: UUID, param: Search): Mono<Page<Type>> = svc.search(workspace, param)

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): Mono<String> {
        ex.printStackTrace()
        return Mono.justOrEmpty(ex.localizedMessage)
    }
}