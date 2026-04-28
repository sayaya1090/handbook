package dev.sayaya.handbook.interfaces.api

import tools.jackson.databind.ObjectMapper
import dev.sayaya.handbook.usecase.Broadcaster
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.time.Duration
import java.util.*

/**
 * SSE(Server-Sent Events)를 통해 워크스페이스별 실시간 이벤트를 스트리밍하는 컨트롤러.
 *
 * **책임:** 클라이언트가 `/workspaces/{workspace}/messages`로 SSE 연결을 맺으면,
 * 해당 워크스페이스의 이벤트를 실시간으로 스트리밍한다.
 * HTTP/1.1 연결 유지를 위해 10초 간격으로 ping 코멘트를 전송한다.
 *
 * **의존관계:**
 * - [Broadcaster] — 워크스페이스별 이벤트 구독
 * - [ObjectMapper] — 이벤트 페이로드 JSON 직렬화
 *
 * **주의:** SSE 이벤트의 id는 이벤트 UUID, event는 EventType, data는 페이로드 JSON이다.
 */
@RestController
class MessageController(
    private val broadcaster: Broadcaster,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val ping = Flux.interval(Duration.ofSeconds(10))
        .map { ServerSentEvent.builder<String>().comment("ping").build() }

    @GetMapping("/workspaces/{workspace}/messages", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun messages(@PathVariable workspace: UUID): Flux<ServerSentEvent<String>> {
        logger.info("SSE connection requested for workspace: {}", workspace)
        return Flux.merge(
            broadcaster.listen(workspace).map { event ->
                ServerSentEvent.builder<String>()
                    .id(event.id.toString())
                    .event(event.eventType.toString())
                    .data(objectMapper.writeValueAsString(event.payload))
                    .retry(Duration.ofSeconds(5))
                    .build()
            },
            ping,
        ).doOnCancel   { logger.info("Client disconnected from workspace: {}", workspace) }
         .doOnComplete { logger.info("SSE stream completed for workspace: {}", workspace) }
         .doOnError    { logger.error("SSE error for workspace {}: {}", workspace, it.message) }
    }
}
