package dev.sayaya.handbook.`interface`.api

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.core.ReactiveAdapterRegistry
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpHeaders.CACHE_CONTROL
import org.springframework.http.HttpStatus
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.method.HandlerMethod
import org.springframework.web.reactive.HandlerResult
import org.springframework.web.reactive.accept.RequestedContentTypeResolver
import org.springframework.web.reactive.config.WebFluxConfigurationSupport
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@SpringBootTest(classes = [
    PageResponseAdviceTest.Companion.TestController::class
]) @Import(WebFluxConfigurationSupport::class)
internal class PageResponseAdviceTest(
    private val controller: TestController,
    private val reactiveAdapterRegistry: ReactiveAdapterRegistry,
    private val serverCodecConfigurer: ServerCodecConfigurer,
    private val contentTypeResolver: RequestedContentTypeResolver
) : ShouldSpec({
    val advice = PageResponseAdvice(
        reactiveAdapterRegistry,
        serverCodecConfigurer.writers,
        contentTypeResolver
    )

    context("supports 메서드 동작 확인") {
        should("Mono<Page> 타입인 경우 true를 반환해야 한다") {
            val method = TestController::class.java.getMethod("getPageResponse")
            val handlerMethod = HandlerMethod(controller, method)
            val handlerResult = HandlerResult(handlerMethod, controller.getPageResponse(), handlerMethod.returnType)

            val result = advice.supports(handlerResult)
            result shouldBe true
        }

        should("Mono<Page>가 아닌 반환 타입인 경우 false를 반환해야 한다") {
            val method = TestController::class.java.getMethod("getNonPageResponse")
            val handlerMethod = HandlerMethod(controller, method)
            val handlerResult = HandlerResult(handlerMethod, controller.getNonPageResponse(), handlerMethod.returnType)

            val result = advice.supports(handlerResult)
            result shouldBe false
        }
    }

    context("handleResult 메서드 동작 확인") {
        should("빈 Page일 경우 상태 코드 204와 적합한 헤더를 반환해야 한다") {
            val method = TestController::class.java.getMethod("getEmptyPageResponse")
            val handlerMethod = HandlerMethod(controller, method)
            val handlerResult = HandlerResult(handlerMethod, controller.getEmptyPageResponse(), handlerMethod.returnType)
            val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))

            StepVerifier.create(advice.handleResult(exchange, handlerResult)).verifyComplete()

            exchange.response.statusCode shouldBe HttpStatus.NO_CONTENT
            exchange.response.headers.getFirst(PageResponseAdvice.HEADER_TOTAL_COUNT) shouldBe "0"
            exchange.response.headers.getFirst(PageResponseAdvice.HEADER_TOTAL_PAGES) shouldBe "0"
        }
        should("Page에 내용이 있을 경우 적합한 헤더를 반환해야 한다") {
            val method = TestController::class.java.getMethod("getNonEmptyPageResponse")
            val handlerMethod = HandlerMethod(controller, method)
            val handlerResult = HandlerResult(handlerMethod, controller.getNonEmptyPageResponse(), handlerMethod.returnType)
            val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))

            StepVerifier.create(advice.handleResult(exchange, handlerResult)).verifyComplete()

            // 상태코드는 클라이언트에서 정의한다
            exchange.response.headers.getFirst(PageResponseAdvice.HEADER_TOTAL_COUNT) shouldBe "3"
            exchange.response.headers.getFirst(PageResponseAdvice.HEADER_TOTAL_PAGES) shouldBe "1"
        }
    }

    context("예외 처리 확인") {
        should("IllegalArgumentException 발생 시 상태 코드 400과 헤더에 에러 메시지를 추가해야 한다") {
            val method = TestController::class.java.getMethod("getErrorPageResponse")
            val handlerMethod = HandlerMethod(controller, method)
            val handlerResult = HandlerResult(handlerMethod, controller.getErrorPageResponse(), handlerMethod.returnType)
            val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))

            StepVerifier.create(advice.handleResult(exchange, handlerResult)).verifyComplete()

            exchange.response.statusCode shouldBe HttpStatus.BAD_REQUEST
            exchange.response.headers.getFirst("Error") shouldBe "잘못된 입력입니다"
        }
        should("기타 예외 발생 시 상태 코드 500과 헤더에 에러 메시지를 추가해야 한다") {
            val method = TestController::class.java.getMethod("getServerErrorPageResponse")
            val handlerMethod = HandlerMethod(controller, method)
            val handlerResult = HandlerResult(handlerMethod, controller.getServerErrorPageResponse(), handlerMethod.returnType)
            val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))

            StepVerifier.create(advice.handleResult(exchange, handlerResult)).verifyComplete()

            exchange.response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
            exchange.response.headers.getFirst("Error") shouldBe "서버 오류 발생"
        }
    }
    context("Cache-Control 헤더 확인") {
        should("응답에 Cache-Control 헤더가 설정되어야 한다") {
            val method = TestController::class.java.getMethod("getEmptyPageResponse")
            val handlerMethod = HandlerMethod(controller, method)
            val handlerResult = HandlerResult(handlerMethod, controller.getEmptyPageResponse(), handlerMethod.returnType)
            val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))

            StepVerifier.create(advice.handleResult(exchange, handlerResult)).verifyComplete()

            exchange.response.headers.getFirst(CACHE_CONTROL) shouldBe "no-cache, no-store, must-revalidate"
        }
    }
}) {
    companion object {
        @RestController
        class TestController {
            fun getPageResponse(): Mono<Page<String>> = Mono.just(PageImpl(listOf("test"), Pageable.unpaged(), 1))
            fun getNonPageResponse(): Mono<String> = Mono.just("test")
            fun getEmptyPageResponse(): Mono<Page<String>> = Mono.just(PageImpl(emptyList(), Pageable.unpaged(), 0))
            fun getNonEmptyPageResponse(): Mono<Page<String>> = Mono.just(PageImpl(listOf("test1", "test2", "test3"), PageRequest.of(0, 10), 3))
            fun getErrorPageResponse(): Mono<Page<String>> = Mono.error(IllegalArgumentException("잘못된 입력입니다"))
            fun getServerErrorPageResponse(): Mono<Page<String>> = Mono.error(RuntimeException("서버 오류 발생"))
        }
    }
}

