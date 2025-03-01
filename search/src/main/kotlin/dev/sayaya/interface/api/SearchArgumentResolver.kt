package dev.sayaya.`interface`.api

import dev.sayaya.domain.Search
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

class SearchArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.parameterType == Search::class.java
    }
    override fun resolveArgument (parameter: MethodParameter, bindingContext: BindingContext, exchange: ServerWebExchange): Mono<Any> {
        val map = exchange.request.queryParams
        val page = map.getFirst("page")?.toIntOrNull() ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing or invalid 'page' parameter")
        val limit = map.getFirst("limit")?.toIntOrNull() ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing or invalid 'limit' parameter")
        val sortBy = map.getFirst("sort_by")
        val asc = map.getFirst("asc")?.toBoolean()
        val filters = map.filterKeys { key -> key !in setOf("page", "limit", "sort_by", "asc") }
            .flatMap { (key, value) -> value.map { key to it } }
            .toMutableList()
        val dto = Search(page, limit, sortBy, asc, filters)
        return Mono.just(dto)
    }
}