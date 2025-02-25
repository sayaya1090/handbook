package dev.sayaya.`interface`.api

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.ReactiveAdapterRegistry
import org.springframework.core.ResolvableType
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.http.HttpHeaders.CACHE_CONTROL
import org.springframework.http.HttpStatus
import org.springframework.http.codec.EncoderHttpMessageWriter
import org.springframework.http.codec.HttpMessageWriter
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.reactive.HandlerResult
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@SpringBootTest
class PageResponseAdviceTest : ShouldSpec({
    val advice = PageResponseAdvice(ReactiveAdapterRegistry.getSharedInstance(), listOf(EncoderHttpMessageWriter(Jackson2JsonEncoder())), mockk())
    context("supports 메서드 동작 확인") {
        should("Mono<Page> 타입인 경우 true를 반환해야 한다") {
            val handlerResult: HandlerResult = mockk()
            val resolveType: ResolvableType = mockk()
            every { handlerResult.returnType } returns resolveType
            every { resolveType.rawClass } returns Mono::class.java

            val pageClass = Page::class.java
            every { resolveType.getGeneric(0).rawClass } returns pageClass

            val result = advice.supports(handlerResult)
            result shouldBe true
        }

        should("Mono<Page>가 아닌 반환 타입인 경우 false를 반환해야 한다") {
            val handlerResult: HandlerResult = mockk()
            val resolveType: ResolvableType = mockk()
            every { handlerResult.returnType } returns resolveType
            every { resolveType.rawClass } returns Mono::class.java

            val result = advice.supports(handlerResult)
            result shouldBe false
        }
    }
/*
    context("handleResult 메서드 동작 확인") {
        should("빈 Page일 경우 상태 코드 204와 적합한 헤더를 반환해야 한다") {
            val exchange = MockServerWebExchange.builder().build()
            val result: HandlerResult = mock()
            val response = exchange.response

            val emptyPage = Page.empty<Any>()
            val returnValue = Mono.just(emptyPage)

            whenever(result.returnValue).thenReturn(returnValue)

            val handle = advice.handleResult(exchange, result)

            StepVerifier.create(handle)
                .verifyComplete()

            response.statusCode shouldBe HttpStatus.NO_CONTENT
            response.headers.getFirst(PageResponseAdvice.HEADER_TOTAL_COUNT) shouldBe "0"
            response.headers.getFirst(PageResponseAdvice.HEADER_TOTAL_PAGES) shouldBe "0"
        }

        should("Page에 데이터가 있을 경우 상태 코드 200과 올바른 헤더를 반환해야 한다") {
            val exchange = MockServerWebExchange.builder().build()
            val result: HandlerResult = mock()
            val response = exchange.response

            val list = listOf("item1", "item2", "item3")
            val page = PageImpl(list)
            val returnValue = Mono.just(page)

            whenever(result.returnValue).thenReturn(returnValue)

            val handle = advice.handleResult(exchange, result)

            StepVerifier.create(handle)
                .verifyComplete()

            response.statusCode shouldBe HttpStatus.OK
            response.headers.getFirst(PageResponseAdvice.HEADER_TOTAL_COUNT) shouldBe "3"
            response.headers.getFirst(PageResponseAdvice.HEADER_TOTAL_PAGES) shouldBe "1"
        }
    }

    context("예외 처리 확인") {
        should("IllegalArgumentException 발생 시 상태 코드 400과 헤더에 에러 메시지를 추가해야 한다") {
            val exchange = MockServerWebExchange.builder().build()
            val result: HandlerResult = mock()
            val response = exchange.response

            val returnValue = Mono.error<Throwable>(IllegalArgumentException("잘못된 입력입니다"))

            whenever(result.returnValue).thenReturn(returnValue)

            val handle = advice.handleResult(exchange, result)

            StepVerifier.create(handle)
                .verifyComplete()

            response.statusCode shouldBe HttpStatus.BAD_REQUEST
            response.headers.getFirst("Error") shouldBe "잘못된 입력입니다"
        }

        should("기타 예외 발생 시 상태 코드 500과 헤더에 에러 메시지를 추가해야 한다") {
            val exchange = MockServerWebExchange.builder().build()
            val result: HandlerResult = mock()
            val response = exchange.response

            val returnValue = Mono.error<Throwable>(RuntimeException("서버 오류 발생"))

            whenever(result.returnValue).thenReturn(returnValue)

            val handle = advice.handleResult(exchange, result)

            StepVerifier.create(handle)
                .verifyComplete()

            response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
            response.headers.getFirst("Error") shouldBe "서버 오류 발생"
        }
    }

    context("Cache-Control 헤더 확인") {
        should("모든 응답에 Cache-Control 헤더가 설정되어야 한다") {
            val exchange = MockServerWebExchange.builder().build()
            val result: HandlerResult = mock()
            val response = exchange.response

            val list = listOf("item1")
            val page = PageImpl(list)
            val returnValue = Mono.just(page)

            whenever(result.returnValue).thenReturn(returnValue)

            val handle = advice.handleResult(exchange, result)

            StepVerifier.create(handle)
                .verifyComplete()

            response.headers.getFirst(CACHE_CONTROL) shouldBe "no-cache, no-store, must-revalidate"
        }
    }
*/
})