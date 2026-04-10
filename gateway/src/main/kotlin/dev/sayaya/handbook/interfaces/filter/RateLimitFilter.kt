package dev.sayaya.handbook.interfaces.filter

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * 인증 엔드포인트에 대한 인메모리 IP 기반 레이트 리미터.
 *
 * **책임:** `/auth` 하위 경로에 대해 IP당 분당 요청 수를 제한한다.
 * 슬라이딩 윈도우 방식으로 1분 간격마다 카운터를 초기화하며,
 * 제한 초과 시 429 Too Many Requests를 반환한다.
 *
 * **의존관계:** 없음 (자체 인메모리 저장소 사용)
 *
 * **주의:** 단일 인스턴스 환경에 적합한 인메모리 구현이다.
 * 분산 환경에서는 Redis 기반 레이트 리미터로 교체해야 한다.
 */
@Component
class RateLimitFilter : WebFilter {

    private val windowMillis = 60_000L
    private val maxRequests = 20

    internal data class WindowCounter(
        val count: AtomicInteger = AtomicInteger(0),
        val windowStart: AtomicLong = AtomicLong(System.currentTimeMillis()),
    )

    internal val counters = ConcurrentHashMap<String, WindowCounter>()

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val path = exchange.request.uri.path
        if (!path.startsWith("/auth/")) return chain.filter(exchange)

        val ip = exchange.request.remoteAddress?.address?.hostAddress ?: "unknown"
        val now = System.currentTimeMillis()

        val counter = counters.compute(ip) { _, existing ->
            if (existing == null || now - existing.windowStart.get() >= windowMillis) {
                WindowCounter(AtomicInteger(0), AtomicLong(now))
            } else {
                existing
            }
        }!!

        val currentCount = counter.count.incrementAndGet()
        if (currentCount > maxRequests) {
            exchange.response.statusCode = HttpStatus.TOO_MANY_REQUESTS
            return exchange.response.setComplete()
        }

        return chain.filter(exchange)
    }
}
