package dev.sayaya.handbook.interfaces.filter

import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.util.*

/**
 * 요청-응답 추적을 위한 Correlation ID 필터.
 *
 * <p><b>책임:</b> 수신 요청에 X-Correlation-Id 헤더가 없으면 UUID를 생성하고,
 * 응답 헤더에 추가하며 MDC에 등록하여 로그 추적을 가능하게 한다.
 * 다운스트림 서비스로 전파하기 위해 요청 헤더에도 추가한다.</p>
 *
 * <p><b>의존관계:</b> 없음</p>
 *
 * <p><b>주의:</b> WebFlux 환경에서 MDC는 스레드별이므로 Reactor Context를 통해
 * correlationId를 전파한다. 이 필터는 가장 먼저 실행되어야 하므로 Ordered.HIGHEST_PRECEDENCE로 설정한다.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class CorrelationIdFilter : WebFilter {

    companion object {
        const val HEADER_NAME = "X-Correlation-Id"
        const val MDC_KEY = "correlationId"
    }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val correlationId = exchange.request.headers.getFirst(HEADER_NAME)
            ?: UUID.randomUUID().toString()

        val mutatedExchange = exchange.mutate()
            .request { builder ->
                builder.header(HEADER_NAME, correlationId)
            }
            .build()

        mutatedExchange.response.headers.set(HEADER_NAME, correlationId)

        return chain.filter(mutatedExchange)
            .contextWrite { ctx -> ctx.put(MDC_KEY, correlationId) }
            .doFirst { MDC.put(MDC_KEY, correlationId) }
            .doFinally { MDC.remove(MDC_KEY) }
    }
}
