package dev.sayaya.handbook.interfaces.event

import com.fasterxml.jackson.databind.ObjectMapper
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.domain.event.Event
import dev.sayaya.handbook.domain.event.TypeEvent
import dev.sayaya.handbook.usecase.TypeEventPublisher
import org.springframework.kafka.core.KafkaTemplate
import java.util.*

/**
 * [TypeEventPublisher] 포트의 Kafka 어댑터.
 *
 * **책임:** 타입 생성/삭제 이벤트를 [TypeEvent]로 래핑하여 Kafka 토픽에 발행한다.
 * 파티션 키는 workspace UUID를 사용하여 동일 워크스페이스 이벤트의 순서를 보장한다.
 *
 * **의존관계:**
 * - [KafkaTemplate] — Kafka 메시지 전송
 * - [ObjectMapper] — 이벤트 JSON 직렬화
 *
 * @param topic 발행 대상 Kafka 토픽 (기본값: "handbook-events")
 */
class KafkaTypeEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    private val topic: String = "handbook-events",
) : TypeEventPublisher {

    override fun publishCreated(workspace: UUID, type: Type) {
        val event = TypeEvent(
            id = UUID.randomUUID(),
            workspace = workspace,
            eventType = Event.EventType.TYPE_CREATED,
            payload = type,
        )
        kafkaTemplate.send(topic, workspace.toString(), objectMapper.writeValueAsString(event))
    }

    override fun publishDeleted(workspace: UUID, type: Type) {
        val event = TypeEvent(
            id = UUID.randomUUID(),
            workspace = workspace,
            eventType = Event.EventType.TYPE_DELETED,
            payload = type,
        )
        kafkaTemplate.send(topic, workspace.toString(), objectMapper.writeValueAsString(event))
    }
}
