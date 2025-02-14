package dev.sayaya.handbook.`interface`.api

import org.springframework.core.ReactiveAdapterRegistry
import org.springframework.http.HttpStatus
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.reactive.HandlerResult
import org.springframework.web.reactive.accept.RequestedContentTypeResolver
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class EmptyFluxResponseAdvice (
    reactiveAdapterRegistry: ReactiveAdapterRegistry,
    serverCodecConfigurer: ServerCodecConfigurer,
    contentTypeResolver: RequestedContentTypeResolver
) : ResponseBodyResultHandler(serverCodecConfigurer.writers, contentTypeResolver, reactiveAdapterRegistry) {
    override fun supports(result: HandlerResult): Boolean {
        return super.supports(result) && hasNoContentOnEmptyAnnotation(result)
    }
    private fun hasNoContentOnEmptyAnnotation(result: HandlerResult): Boolean {
        val handlerMethod = result.handler as? HandlerMethod
        return handlerMethod?.hasMethodAnnotation(ResponseStatusNoContentOnEmpty::class.java) == true
    }
    override fun handleResult(exchange: ServerWebExchange, result: HandlerResult): Mono<Void> = when (val body = result.returnValue) {
        is Mono<*> -> handleMonoBody(exchange, result, body)
        is Flux<*> -> handleFluxBody(exchange, result, body)
        else -> super.handleResult(exchange, result)
    }
    private fun handleMonoBody(exchange: ServerWebExchange, result: HandlerResult, body: Mono<*>): Mono<Void> = body.switchIfEmpty (
        Mono.defer {
            exchange.response.statusCode = HttpStatus.NO_CONTENT
            Mono.empty()
        }
    ).flatMap {
        super.handleResult(exchange, result)
    }

    private fun handleFluxBody(exchange: ServerWebExchange, result: HandlerResult, body: Flux<*>): Mono<Void> = body.hasElements().flatMap { hasElements ->
        if (!hasElements) {
            exchange.response.statusCode = HttpStatus.NO_CONTENT
            Mono.empty()
        } else super.handleResult(exchange, result)
    }
}