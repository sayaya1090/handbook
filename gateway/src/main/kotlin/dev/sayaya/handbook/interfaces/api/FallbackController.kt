package dev.sayaya.handbook.interfaces.api

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 선택 서비스(assistant, event-broadcaster) 장애 시 빈 응답을 반환하는 폴백 컨트롤러.
 *
 * **책임:** CircuitBreaker 필터가 fallbackUri로 라우팅할 때 502 대신 빈 JSON 응답을 반환하여
 * 클라이언트가 핵심 기능을 계속 사용할 수 있도록 한다.
 *
 * **의존관계:** 없음 (독립 컨트롤러)
 *
 * **주의:** 폴백이 호출되면 경고 로그를 남긴다. 모니터링 시스템에서 이 로그를 추적해야 한다.
 */
@RestController
class FallbackController {
    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("/fallback/empty")
    fun fallbackGet(): ResponseEntity<Map<String, Any>> {
        logger.warn("Circuit breaker fallback triggered (GET)")
        return ResponseEntity.status(HttpStatus.OK)
            .body(mapOf("fallback" to true, "data" to emptyList<Any>()))
    }

    @PostMapping("/fallback/empty")
    fun fallbackPost(): ResponseEntity<Map<String, Any>> {
        logger.warn("Circuit breaker fallback triggered (POST)")
        return ResponseEntity.status(HttpStatus.OK)
            .body(mapOf("fallback" to true, "data" to emptyList<Any>()))
    }
}
