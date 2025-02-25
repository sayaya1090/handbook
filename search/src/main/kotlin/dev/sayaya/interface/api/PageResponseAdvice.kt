package dev.sayaya.`interface`.api

import org.springframework.core.ReactiveAdapterRegistry
import org.springframework.data.domain.Page
import org.springframework.http.HttpHeaders.CACHE_CONTROL
import org.springframework.http.HttpStatus
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.reactive.HandlerResult
import org.springframework.web.reactive.accept.RequestedContentTypeResolver
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class PageResponseAdvice(
    reactiveAdapterRegistry: ReactiveAdapterRegistry,
    serverCodecConfigurer: ServerCodecConfigurer,
    contentTypeResolver: RequestedContentTypeResolver
) : ResponseBodyResultHandler(serverCodecConfigurer.writers, contentTypeResolver, reactiveAdapterRegistry) {
    companion object {
        const val HEADER_TOTAL_COUNT = "Total-Count"
        const val HEADER_TOTAL_PAGES = "Total-Pages"
    }
    override fun supports(result: HandlerResult): Boolean {
        val returnType = result.returnType
        return if (returnType.hasGenerics() && returnType.rawClass == Mono::class.java) {
            val genericType = returnType.getGeneric(0).rawClass
            genericType == Page::class.java
        } else false
    }
    override fun handleResult(exchange: ServerWebExchange, result: HandlerResult): Mono<Void> {
        val response = exchange.response
        val returnValue = result.returnValue
        return if (returnValue is Mono<*>) handleMonoResult(returnValue, response, result, exchange)
        else super.handleResult(exchange, result)
    }
    private fun handleMonoResult(returnValue: Mono<*>, response: ServerHttpResponse, result: HandlerResult, exchange: ServerWebExchange): Mono<Void> = returnValue.switchIfEmpty(emptyResult(response))
        .cast(Page::class.java)
        .flatMap { page -> handleNonEmptyPage(page, response, result, exchange) }
        .onErrorResume { ex -> handleError(response, ex) }

    private fun handleNonEmptyPage(page: Page<*>, response: ServerHttpResponse, result: HandlerResult, exchange: ServerWebExchange): Mono<Void> {
        val list = page.toList()
        return if (list.isEmpty()) emptyResult(response).then()
        else {
            handlePageHeaders(response, page.totalElements, page.totalPages)
            writeBody(list, result.returnTypeSource.nested(), exchange)
        }
    }
    private fun handleError(response: ServerHttpResponse, ex: Throwable): Mono<Void> = Mono.fromRunnable<Void> {
        when (ex) {
            is IllegalArgumentException -> response.setStatusCode(HttpStatus.BAD_REQUEST) // 잘못된 요청인 경우
            else -> response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR)
        }
        response.headers.add("Error", ex.message ?: "Unknown error occurred")
    }.also { ex.printStackTrace() }

    private fun emptyResult(response: ServerHttpResponse): Mono<out Nothing> = Mono.fromRunnable {
        handlePageHeaders(response, 0, 0)
        response.setStatusCode(HttpStatus.NO_CONTENT)
    }
    private fun handlePageHeaders(response: ServerHttpResponse, totalCount: Long, totalPages: Int, cacheControl: String = "no-cache, no-store, must-revalidate") {
        response.headers.set(HEADER_TOTAL_COUNT, totalCount.toString())
        response.headers.set(HEADER_TOTAL_PAGES, totalPages.toString())
        response.headers.set(CACHE_CONTROL, cacheControl)
    }
}