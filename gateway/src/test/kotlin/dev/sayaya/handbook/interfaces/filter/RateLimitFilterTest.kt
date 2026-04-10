package dev.sayaya.handbook.interfaces.filter

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

class RateLimitFilterTest : DescribeSpec({

    describe("RateLimitFilter는") {
        it("auth 이외 경로는 제한하지 않는다") {
            val filter = RateLimitFilter()
            val request = MockServerHttpRequest.get("/workspace/test").build()
            val exchange = MockServerWebExchange.from(request)
            var chainCalled = false
            val chain = WebFilterChain { _ ->
                chainCalled = true
                Mono.empty()
            }
            filter.filter(exchange, chain).block()
            chainCalled shouldBe true
        }

        it("auth 경로에 대해 제한 내 요청은 통과시킨다") {
            val filter = RateLimitFilter()
            val request = MockServerHttpRequest.get("/auth/login").build()
            val exchange = MockServerWebExchange.from(request)
            var chainCalled = false
            val chain = WebFilterChain { _ ->
                chainCalled = true
                Mono.empty()
            }
            filter.filter(exchange, chain).block()
            chainCalled shouldBe true
        }

        it("auth 경로에 대해 제한 초과 시 429를 반환한다") {
            val filter = RateLimitFilter()
            val chain = WebFilterChain { _ -> Mono.empty() }

            // 20회 요청 — 모두 통과
            repeat(20) {
                val request = MockServerHttpRequest.get("/auth/login").build()
                val exchange = MockServerWebExchange.from(request)
                filter.filter(exchange, chain).block()
            }

            // 21번째 요청 — 429
            val request = MockServerHttpRequest.get("/auth/login").build()
            val exchange = MockServerWebExchange.from(request)
            filter.filter(exchange, chain).block()
            exchange.response.statusCode shouldBe HttpStatus.TOO_MANY_REQUESTS
        }

        it("윈도우가 경과하면 카운터가 초기화된다") {
            val filter = RateLimitFilter()
            val chain = WebFilterChain { _ -> Mono.empty() }

            // 20회 요청
            repeat(20) {
                val request = MockServerHttpRequest.get("/auth/login").build()
                val exchange = MockServerWebExchange.from(request)
                filter.filter(exchange, chain).block()
            }

            // 윈도우 시작 시간을 과거로 조작
            filter.counters.values.forEach { counter ->
                counter.windowStart.set(System.currentTimeMillis() - 61_000L)
            }

            // 다음 요청은 새 윈도우로 통과
            var chainCalled = false
            val resetChain = WebFilterChain { _ ->
                chainCalled = true
                Mono.empty()
            }
            val request = MockServerHttpRequest.get("/auth/login").build()
            val exchange = MockServerWebExchange.from(request)
            filter.filter(exchange, resetChain).block()
            chainCalled shouldBe true
        }
    }
})
