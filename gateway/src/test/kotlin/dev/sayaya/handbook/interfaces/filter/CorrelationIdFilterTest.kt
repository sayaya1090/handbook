package dev.sayaya.handbook.interfaces.filter

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldMatch
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

class CorrelationIdFilterTest : DescribeSpec({

    describe("CorrelationIdFilter는") {
        val filter = CorrelationIdFilter()

        it("요청에 X-Correlation-Id가 없으면 UUID를 생성하여 응답 헤더에 추가한다") {
            val request = MockServerHttpRequest.get("/test").build()
            val exchange = MockServerWebExchange.from(request)
            val chain = WebFilterChain { _ -> Mono.empty() }

            filter.filter(exchange, chain).block()

            val correlationId = exchange.response.headers.getFirst(CorrelationIdFilter.HEADER_NAME)
            correlationId shouldNotBe null
            correlationId!! shouldMatch "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
        }

        it("요청에 X-Correlation-Id가 있으면 그대로 전파한다") {
            val existingId = "existing-correlation-id-123"
            val request = MockServerHttpRequest.get("/test")
                .header(CorrelationIdFilter.HEADER_NAME, existingId)
                .build()
            val exchange = MockServerWebExchange.from(request)
            val chain = WebFilterChain { _ -> Mono.empty() }

            filter.filter(exchange, chain).block()

            val correlationId = exchange.response.headers.getFirst(CorrelationIdFilter.HEADER_NAME)
            correlationId shouldBe existingId
        }

        it("다운스트림 요청 헤더에 correlationId를 추가한다") {
            val request = MockServerHttpRequest.get("/test").build()
            val exchange = MockServerWebExchange.from(request)
            var downstreamHeaderValue: String? = null
            val chain = WebFilterChain { ex ->
                downstreamHeaderValue = ex.request.headers.getFirst(CorrelationIdFilter.HEADER_NAME)
                Mono.empty()
            }

            filter.filter(exchange, chain).block()

            downstreamHeaderValue shouldNotBe null
            downstreamHeaderValue!! shouldMatch "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
        }
    }
})
