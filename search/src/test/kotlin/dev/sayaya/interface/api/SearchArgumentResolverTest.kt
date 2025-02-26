package dev.sayaya.`interface`.api

import dev.sayaya.domain.Search
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.BindingContext
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.test.StepVerifier

class SearchArgumentResolverTest : ShouldSpec({
    val resolver = SearchArgumentResolver()
    context("supportsParameter 테스트") {
        should("Search 타입 파라미터를 지원해야 함") {
            val parameter = mockk<MethodParameter>()
            every { parameter.parameterType } returns Search::class.java
            resolver.supportsParameter(parameter) shouldBe true
        }

        should("Search 타입이 아닌 파라미터는 지원하지 않아야 함") {
            val parameter = mockk<MethodParameter>()
            every { parameter.parameterType } returns String::class.java
            resolver.supportsParameter(parameter) shouldBe false
        }
    }

    context("resolveArgument 테스트") {
        val parameter = mockk<MethodParameter>()
        val bindingContext = mockk<BindingContext>()

        context("올바른 파라미터로 요청할 경우") {
            val exchange = mockk<ServerWebExchange>()
            val request = mockk<ServerHttpRequest>()
            val queryParams = MultiValueMap.fromSingleValue(mapOf(
                "page" to "0",
                "limit" to "10",
                "sort_by" to "name",
                "asc" to "true",
                "status" to "active"
            ))

            beforeTest {
                every { exchange.request } returns request
                every { request.queryParams } returns queryParams
            }

            should("올바른 Search 객체를 반환해야 함") {
                resolver.resolveArgument(parameter, bindingContext, exchange)
                    .let(StepVerifier::create).expectNext(
                        Search(page = 0,
                            limit = 10,
                            sortBy = "name",
                            asc = true,
                            filters = mutableListOf("status" to "active"))
                    ).verifyComplete()
            }
        }

        context("필수 파라미터가 누락된 경우") {
            should("page가 누락되면 BAD_REQUEST 예외를 던져야 함") {
                val exchange = mockk<ServerWebExchange>()
                val request = mockk<ServerHttpRequest>()
                val queryParams = MultiValueMap.fromSingleValue(mapOf(
                    "limit" to "10"
                ))

                every { exchange.request } returns request
                every { request.queryParams } returns queryParams

                shouldThrow<ResponseStatusException> {
                    resolver.resolveArgument(parameter, bindingContext, exchange).block()
                }.statusCode shouldBe HttpStatus.BAD_REQUEST
            }

            should("limit이 누락되면 BAD_REQUEST 예외를 던져야 함") {
                val exchange = mockk<ServerWebExchange>()
                val request = mockk<ServerHttpRequest>()
                val queryParams = MultiValueMap.fromSingleValue(mapOf(
                    "page" to "0"
                ))

                every { exchange.request } returns request
                every { request.queryParams } returns queryParams

                shouldThrow<ResponseStatusException> {
                    resolver.resolveArgument(parameter, bindingContext, exchange).block()
                }.statusCode shouldBe HttpStatus.BAD_REQUEST
            }
        }

        context("잘못된 형식의 파라미터가 전달된 경우") {
            should("page가 숫자가 아니면 BAD_REQUEST 예외를 던져야 함") {
                val exchange = mockk<ServerWebExchange>()
                val request = mockk<ServerHttpRequest>()
                val queryParams = MultiValueMap.fromSingleValue(mapOf(
                    "page" to "invalid",
                    "limit" to "10"
                ))

                every { exchange.request } returns request
                every { request.queryParams } returns queryParams

                shouldThrow<ResponseStatusException> {
                    resolver.resolveArgument(parameter, bindingContext, exchange).block()
                }.statusCode shouldBe HttpStatus.BAD_REQUEST
            }
        }
    }
})