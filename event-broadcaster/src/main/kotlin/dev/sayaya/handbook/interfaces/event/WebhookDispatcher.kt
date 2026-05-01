package dev.sayaya.handbook.interfaces.event

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import tools.jackson.databind.ObjectMapper
import java.util.function.Consumer

/**
 * Kafka 이벤트를 수신하여 등록된 웹훅 URL로 콜백을 전송하는 디스패처.
 *
 * <p><b>책임:</b> Spring Cloud Stream Consumer 바인딩("webhook")으로 Kafka 이벤트를 수신하고,
 * 이벤트 JSON을 파싱하여 workspace/eventType을 추출한 뒤,
 * workspace-command API로 매칭 웹훅을 조회하고 [WebhookSender]에 전송을 위임한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>[ObjectMapper] — 이벤트 JSON 파싱</li>
 *   <li>[WebClient] — 웹훅 목록 조회</li>
 *   <li>[WebhookSender] — HTTP POST 콜백 전송 (재시도 포함)</li>
 * </ul></p>
 *
 * <p><b>주의:</b> webhook.service.url 프로퍼티로 workspace-command API 기본 URL을 설정한다.
 * 이벤트 파싱 실패 시 해당 이벤트를 무시하고 로그를 남긴다.</p>
 */
@Component("webhook")
class WebhookDispatcher(
    private val objectMapper: ObjectMapper,
    @Value("\${webhook.service.url:http://localhost:8080}") private val webhookServiceUrl: String,
    private val webhookSender: WebhookSender = WebhookSender(),
) : Consumer<String> {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val webClient: WebClient = WebClient.builder().build()

    override fun accept(event: String) {
        val parsed = parseEvent(event) ?: return
        val workspace = parsed["workspace"] as? String ?: return
        val eventType = parsed["event_type"] as? String ?: return

        fetchWebhooks(workspace, eventType)
            .doOnNext { webhookUrl -> webhookSender.send(webhookUrl, event) }
            .subscribe()
    }

    /**
     * 이벤트 JSON을 맵으로 파싱한다.
     */
    private fun parseEvent(event: String): Map<String, Any>? {
        return try {
            @Suppress("UNCHECKED_CAST")
            objectMapper.readValue(event, Map::class.java) as Map<String, Any>
        } catch (e: Exception) {
            logger.warn("Failed to parse event JSON: {}", e.message)
            null
        }
    }

    /**
     * workspace-command API에서 매칭되는 웹훅 URL 목록을 조회한다.
     */
    private fun fetchWebhooks(workspace: String, eventType: String) = webClient
        .get()
        .uri("$webhookServiceUrl/workspace/$workspace/webhooks")
        .accept(MediaType.parseMediaType("application/vnd.sayaya.handbook.v1+json"))
        .retrieve()
        .bodyToFlux(WebhookResponse::class.java)
        .filter { wh -> wh.active && (wh.events.isEmpty() || eventType in wh.events) }
        .map { it.url }
        .onErrorResume { e ->
            logger.warn("Failed to fetch webhooks for workspace {}: {}", workspace, e.message)
            reactor.core.publisher.Flux.empty()
        }

    /**
     * workspace-command 웹훅 API 응답 매핑용 내부 DTO.
     *
     * **책임:** WebClient로 조회한 웹훅 JSON 응답을 역직렬화한다.
     *
     * @property url 콜백 URL
     * @property events 구독 이벤트 목록
     * @property active 활성 여부
     */
    private data class WebhookResponse(
        val url: String = "",
        val events: List<String> = emptyList(),
        val active: Boolean = true,
    )
}
