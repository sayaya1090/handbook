package dev.sayaya.`interface`.api

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.core.ReactiveAdapterRegistry
import org.springframework.core.ResolvableType
import org.springframework.data.domain.Page
import org.springframework.http.HttpHeaders.CACHE_CONTROL
import org.springframework.http.HttpStatus
import org.springframework.http.codec.EncoderHttpMessageWriter
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.reactive.HandlerResult
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@Suppress("ReactiveStreamsUnusedPublisher")
class PageResponseAdviceTest : ShouldSpec({
    val advice = PageResponseAdvice(
        ReactiveAdapterRegistry.getSharedInstance(),
        listOf(EncoderHttpMessageWriter(Jackson2JsonEncoder())),
        mockk()
    )

    context("supports 메서드 동작 확인") {
        should("Mono<Page> 타입인 경우 true를 반환해야 한다") {
            val handlerResult = mockk<HandlerResult>().apply {
                val resolveType = mockk<ResolvableType>().apply {
                    every { rawClass } returns Mono::class.java
                    every { hasGenerics() } returns true
                    every { getGeneric(0).rawClass } returns Page::class.java
                }
                every { returnType } returns resolveType
            }
            val result = advice.supports(handlerResult)
            result shouldBe true
        }
        should("Mono<Page>가 아닌 반환 타입인 경우 false를 반환해야 한다") {
            val handlerResult = mockk<HandlerResult>().apply {
                val resolveType = mockk<ResolvableType>().apply {
                    every { rawClass } returns Mono::class.java
                    every { hasGenerics() } returns true
                    every { getGeneric(0).rawClass } returns Any::class.java
                }
                every { returnType } returns resolveType
            }
            val result = advice.supports(handlerResult)
            result shouldBe false
        }
    }
    context("handleResult 메서드 동작 확인") {
        should("빈 Page일 경우 상태 코드 204와 적합한 헤더를 반환해야 한다") {
            val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))
            val result: HandlerResult = mockk<HandlerResult>().apply {
                every { returnValue } returns Mono.just(Page.empty<Any>())
            }
            val response = exchange.response
            val handle = advice.handleResult(exchange, result)

            StepVerifier.create(handle).verifyComplete()

            response.statusCode shouldBe HttpStatus.NO_CONTENT
            response.headers.getFirst(PageResponseAdvice.HEADER_TOTAL_COUNT) shouldBe "0"
            response.headers.getFirst(PageResponseAdvice.HEADER_TOTAL_PAGES) shouldBe "0"
        }
    }
    context("예외 처리 확인") {
        should("IllegalArgumentException 발생 시 상태 코드 400과 헤더에 에러 메시지를 추가해야 한다") {
            val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))
            val result: HandlerResult = mockk<HandlerResult>().apply {
                every { returnValue } returns  Mono.error<Throwable>(IllegalArgumentException("잘못된 입력입니다"))
            }
            val response = exchange.response
            val handle = advice.handleResult(exchange, result)

            StepVerifier.create(handle).verifyComplete()
            response.statusCode shouldBe HttpStatus.BAD_REQUEST
            response.headers.getFirst("Error") shouldBe "잘못된 입력입니다"
        }
        should("기타 예외 발생 시 상태 코드 500과 헤더에 에러 메시지를 추가해야 한다") {
            val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))
            val result: HandlerResult = mockk<HandlerResult>().apply {
                every { returnValue } returns Mono.error<Throwable>(RuntimeException("서버 오류 발생"))
            }
            val response = exchange.response
            val handle = advice.handleResult(exchange, result)

            StepVerifier.create(handle).verifyComplete()
            response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
            response.headers.getFirst("Error") shouldBe "서버 오류 발생"
        }
    }
    context("Cache-Control 헤더 확인") {
        should("모든 응답에 Cache-Control 헤더가 설정되어야 한다") {
            val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))
            val result: HandlerResult = mockk<HandlerResult>().apply {
                every { returnValue } returns Mono.just(Page.empty<Any>())
            }
            val response = exchange.response
            val handle = advice.handleResult(exchange, result)
            StepVerifier.create(handle).verifyComplete()
            response.headers.getFirst(CACHE_CONTROL) shouldBe "no-cache, no-store, must-revalidate"
        }
    }
})
