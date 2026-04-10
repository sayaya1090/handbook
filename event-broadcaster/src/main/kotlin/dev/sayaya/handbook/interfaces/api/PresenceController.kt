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
 * 프레즌스(편집 위치 공유) REST 엔드포인트.
 *
 * **책임:** POST /workspace/{id}/presence 요청을 받아 PresenceEvent를 Kafka로 발행.
 * 모든 event-broadcaster 인스턴스가 수신하여 SSE로 브로드캐스트한다.
 *
 * **의존관계:**
 * - [KafkaTemplate] — handbook-events 토픽 발행 (파티션 키: workspace UUID)
 * - [ObjectMapper] — PresenceEvent JSON 직렬화
 *
 * **주의:** DB 저장 없이 stateless. 타임아웃(30초)은 클라이언트에서 처리.
 * 프레즌스 해제 시 payload.type을 null로 전송한다.
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
