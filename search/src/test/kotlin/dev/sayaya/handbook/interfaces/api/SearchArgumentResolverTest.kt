package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Search
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.BindingContext
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.test.StepVerifier

class SearchArgumentResolverTest : BehaviorSpec({
    val resolver = SearchArgumentResolver()

    Given("supportsParameter 검증") {
        When("파라미터 타입이 Search이면") {
            val parameter = mockk<MethodParameter>()
            every { parameter.parameterType } returns Search::class.java

            Then("true를 반환한다") {
                resolver.supportsParameter(parameter) shouldBe true
            }
        }

        When("파라미터 타입이 Search가 아니면") {
            val parameter = mockk<MethodParameter>()
            every { parameter.parameterType } returns String::class.java

            Then("false를 반환한다") {
                resolver.supportsParameter(parameter) shouldBe false
            }
        }
    }

    Given("유효한 쿼리 파라미터가 주어졌을 때") {
        val queryParams = LinkedMultiValueMap<String, String>().apply {
            add("page", "0")
            add("limit", "10")
            add("sort_by", "name")
            add("asc", "true")
            add("type", "customer")
        }
        val exchange = mockk<ServerWebExchange>()
        val request = mockk<ServerHttpRequest>()
        every { exchange.request } returns request
        every { request.queryParams } returns queryParams

        val parameter = mockk<MethodParameter>()
        val bindingContext = mockk<BindingContext>()

        When("resolveArgument를 호출하면") {
            val result = resolver.resolveArgument(parameter, bindingContext, exchange)

            Then("Search 객체가 올바르게 생성된다") {
                StepVerifier.create(result)
                    .assertNext { arg ->
                        val search = arg as Search
                        search.page shouldBe 0
                        search.limit shouldBe 10
                        search.sortBy shouldBe "name"
                        search.asc shouldBe true
                        search.filters.size shouldBe 1
                        search.filters[0] shouldBe ("type" to "customer")
                    }
                    .verifyComplete()
            }
        }
    }

    Given("page 파라미터가 없을 때") {
        val queryParams = LinkedMultiValueMap<String, String>().apply {
            add("limit", "10")
        }
        val exchange = mockk<ServerWebExchange>()
        val request = mockk<ServerHttpRequest>()
        every { exchange.request } returns request
        every { request.queryParams } returns queryParams

        val parameter = mockk<MethodParameter>()
        val bindingContext = mockk<BindingContext>()

        When("resolveArgument를 호출하면") {
            Then("400 Bad Request가 발생한다") {
                val ex = shouldThrow<ResponseStatusException> {
                    resolver.resolveArgument(parameter, bindingContext, exchange)
                }
                ex.statusCode shouldBe HttpStatus.BAD_REQUEST
            }
        }
    }

    Given("limit 파라미터가 없을 때") {
        val queryParams = LinkedMultiValueMap<String, String>().apply {
            add("page", "0")
        }
        val exchange = mockk<ServerWebExchange>()
        val request = mockk<ServerHttpRequest>()
        every { exchange.request } returns request
        every { request.queryParams } returns queryParams

        val parameter = mockk<MethodParameter>()
        val bindingContext = mockk<BindingContext>()

        When("resolveArgument를 호출하면") {
            Then("400 Bad Request가 발생한다") {
                val ex = shouldThrow<ResponseStatusException> {
                    resolver.resolveArgument(parameter, bindingContext, exchange)
                }
                ex.statusCode shouldBe HttpStatus.BAD_REQUEST
            }
        }
    }

    Given("sort_by와 asc 없이 조회할 때") {
        val queryParams = LinkedMultiValueMap<String, String>().apply {
            add("page", "1")
            add("limit", "50")
        }
        val exchange = mockk<ServerWebExchange>()
        val request = mockk<ServerHttpRequest>()
        every { exchange.request } returns request
        every { request.queryParams } returns queryParams

        val parameter = mockk<MethodParameter>()
        val bindingContext = mockk<BindingContext>()

        When("resolveArgument를 호출하면") {
            val result = resolver.resolveArgument(parameter, bindingContext, exchange)

            Then("sortBy와 asc가 null인 Search가 생성된다") {
                StepVerifier.create(result)
                    .assertNext { arg ->
                        val search = arg as Search
                        search.page shouldBe 1
                        search.limit shouldBe 50
                        search.sortBy shouldBe null
                        search.asc shouldBe null
                        search.filters shouldBe emptyList()
                    }
                    .verifyComplete()
            }
        }
    }
})
