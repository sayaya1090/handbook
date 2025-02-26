package dev.sayaya.handbook.`interface`.api

import dev.sayaya.domain.Search
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.usecase.TypeSearchService
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class SearchController(private val svc: TypeSearchService) {
    @GetMapping("/types", produces = ["application/vnd.sayaya.handbook.v1+json"])
    fun findWorklists(param: Search): Mono<Page<Type>> = svc.search(param)

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): Mono<String> {
        ex.printStackTrace()
        return Mono.justOrEmpty(ex.localizedMessage)
    }
}