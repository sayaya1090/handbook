package dev.sayaya.handbook.interfaces.event

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.time.Duration

/**
 * 웹훅 URL에 이벤트 페이로드를 HTTP POST로 전송하는 발신기.
 *
 * <p><b>책임:</b> 지정된 URL에 JSON 페이로드를 전송하고, 실패 시 지수 백오프로 재시도한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>[WebClient] — HTTP POST 요청 수행</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 최대 3회 재시도(지수 백오프, 1초 base) 후에도 실패하면 구조화된 로그와
 * Micrometer 카운터({@code webhook_failures_total})를 기록하고 에러를 삼킨다(onErrorResume).
 * 호출자의 이벤트 스트림이 중단되지 않도록 설계되었다.</p>
 */
class WebhookSender(
    private val webClient: WebClient = WebClient.builder().build(),
    private val maxRetries: Long = 3,
    private val retryBackoff: Duration = Duration.ofSeconds(1),
    meterRegistry: MeterRegistry? = null,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val failureCounter: Counter? = meterRegistry?.let {
        Counter.builder("webhook_failures_total")
            .description("Total number of webhook delivery failures after all retries")
            .register(it)
    }

    /**
     * 대상 URL에 이벤트 페이로드를 HTTP POST로 전송한다.
     * 최대 [maxRetries]회 재시도, 지수 백오프([retryBackoff] base).
     */
    fun send(url: String, payload: String, eventType: String? = null) {
        webClient
            .post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .retrieve()
            .toBodilessEntity()
            .retryWhen(Retry.backoff(maxRetries, retryBackoff))
            .doOnSuccess { logger.info("Webhook callback sent to {}", url) }
            .doOnError { e ->
                failureCounter?.increment()
                logger.error(
                    "Webhook delivery failed: url={}, eventType={}, error={}",
                    url, eventType ?: "unknown", e.message,
                )
            }
            .onErrorResume { Mono.empty() }
            .subscribe()
    }
}
