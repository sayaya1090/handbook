package dev.sayaya.handbook.interfaces.api

import com.fasterxml.jackson.databind.ObjectMapper
import dev.sayaya.handbook.domain.event.Event
import dev.sayaya.handbook.domain.event.PresenceEvent
import dev.sayaya.handbook.domain.event.PresencePayload
import org.springframework.http.HttpStatus
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*

/**
 * 프레즌스(편집 위치 공유) 엔드포인트.
 * POST 요청을 받아 Kafka로 발행하면, 모든 event-broadcaster 인스턴스가
 * 수신하여 SSE로 브로드캐스트한다.
 */
@RestController
class PresenceController(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
) {
    @PostMapping("/workspace/{workspace}/presence")
    @ResponseStatus(HttpStatus.OK)
    fun publish(
        @PathVariable workspace: UUID,
        @RequestBody payload: PresencePayload,
    ): Mono<Void> {
        val event = PresenceEvent(workspace = workspace, payload = payload)
        val json = objectMapper.writeValueAsString(event)
        return Mono.fromFuture(kafkaTemplate.send("handbook-events", workspace.toString(), json).toCompletableFuture())
            .then()
    }
}
