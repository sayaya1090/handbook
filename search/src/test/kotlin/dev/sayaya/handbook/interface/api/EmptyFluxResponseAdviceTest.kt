package dev.sayaya.`interface`.api

import dev.sayaya.handbook.`interface`.api.EmptyFluxResponseAdvice
import dev.sayaya.handbook.`interface`.api.ResponseStatusNoContentOnEmpty
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.core.ReactiveAdapterRegistry
import org.springframework.http.HttpStatus
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.method.HandlerMethod
import org.springframework.web.reactive.HandlerResult
import org.springframework.web.reactive.accept.RequestedContentTypeResolver
import org.springframework.web.reactive.config.WebFluxConfigurationSupport
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@SpringBootTest(classes = [
    EmptyFluxResponseAdviceTest.Companion.TestController::class
]) @Import(WebFluxConfigurationSupport::class)
internal class EmptyFluxResponseAdviceTest(
    private val controller: TestController,
    private val reactiveAdapterRegistry: ReactiveAdapterRegistry,
    private val serverCodecConfigurer: ServerCodecConfigurer,
    private val contentTypeResolver: RequestedContentTypeResolver
) : ShouldSpec({
    val advice = EmptyFluxResponseAdvice(
        reactiveAdapterRegistry,
        serverCodecConfigurer,
        contentTypeResolver
    )
    context("supports 메서드 동작 확인") {
        should("@ResponseStatusNoContentOnEmpty 어노테이션이 있으면 true를 반환해야 한다") {
            val method = TestController::class.java.getMethod("nonEmptyMonoResponse")
            val handlerMethod = HandlerMethod(controller, method)
            val handlerResult = HandlerResult(handlerMethod, controller.nonEmptyMonoResponse(), handlerMethod.returnType)

            val result = advice.supports(handlerResult)
            result shouldBe true
        }

        should("@ResponseStatusNoContentOnEmpty 어노테이션이 없으면 false를 반환해야 한다") {
            val method = TestController::class.java.getMethod("nonAnnotatedMethod")
            val handlerMethod = HandlerMethod(controller, method)
            val handlerResult = HandlerResult(handlerMethod, controller.nonAnnotatedMethod(), handlerMethod.returnType)

            val result = advice.supports(handlerResult)
            result shouldBe false
        }
    }

    context("handleResult 메서드 동작 확인") {
        should("빈 Mono를 반환할 경우 204 상태 코드를 반환해야 한다") {
            val method = TestController::class.java.getMethod("emptyMonoResponse")
            val handlerMethod = HandlerMethod(controller, method)
            val handlerResult = HandlerResult(handlerMethod, controller.emptyMonoResponse(), handlerMethod.returnType)
            val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))

            StepVerifier.create(advice.handleResult(exchange, handlerResult)).verifyComplete()
            exchange.response.statusCode shouldBe HttpStatus.NO_CONTENT
        }

        should("빈 Flux를 반환할 경우 204 상태 코드를 반환해야 한다") {
            val method = TestController::class.java.getMethod("emptyFluxResponse")
            val handlerMethod = HandlerMethod(controller, method)
            val handlerResult = HandlerResult(handlerMethod, controller.emptyFluxResponse(), handlerMethod.returnType)
            val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))

            StepVerifier.create(advice.handleResult(exchange, handlerResult)).verifyComplete()
            exchange.response.statusCode shouldBe HttpStatus.NO_CONTENT
        }

        should("값이 있는 Mono를 반환할 경우 기본 처리를 수행해야 한다") {
            val method = TestController::class.java.getMethod("nonEmptyMonoResponse")
            val handlerMethod = HandlerMethod(controller, method)
            val handlerResult = HandlerResult(handlerMethod,controller.nonEmptyMonoResponse(), handlerMethod.returnType)
            val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))

            StepVerifier.create(advice.handleResult(exchange, handlerResult)).verifyComplete()
            exchange.response.statusCode shouldNotBe HttpStatus.NO_CONTENT
        }

        should("값이 있는 Flux를 반환할 경우 기본 처리를 수행해야 한다") {
            val method = TestController::class.java.getMethod("nonEmptyFluxResponse")
            val handlerMethod = HandlerMethod(controller, method)
            val handlerResult = HandlerResult(handlerMethod, controller.nonEmptyFluxResponse(), handlerMethod.returnType)
            val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))

            StepVerifier.create(advice.handleResult(exchange, handlerResult)).verifyComplete()
            exchange.response.statusCode shouldNotBe HttpStatus.NO_CONTENT
        }

        should("반환타입이 Mono, Flux가 아닌 경우 예외를 리턴해야 한다") {
            val method = TestController::class.java.getMethod("unsupportedResponse")
            val handlerMethod = HandlerMethod(controller, method)
            val handlerResult = HandlerResult(handlerMethod, controller.unsupportedResponse(), handlerMethod.returnType)
            val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))

            StepVerifier.create(advice.handleResult(exchange, handlerResult)).expectError(IllegalArgumentException::class.java).verify()
        }
    }
}) {
    companion object {
        @RestController
        class TestController {
            fun nonAnnotatedMethod(): String = "test"

            @ResponseStatusNoContentOnEmpty
            fun emptyMonoResponse(): Mono<String> = Mono.empty()

            @ResponseStatusNoContentOnEmpty
            fun nonEmptyMonoResponse(): Mono<String> = Mono.just("test")

            @ResponseStatusNoContentOnEmpty
            fun emptyFluxResponse(): Flux<String> = Flux.empty()

            @ResponseStatusNoContentOnEmpty
            fun nonEmptyFluxResponse(): Flux<String> = Flux.just("test")

            @ResponseStatusNoContentOnEmpty
            fun unsupportedResponse(): String = "test"
        }
    }
}