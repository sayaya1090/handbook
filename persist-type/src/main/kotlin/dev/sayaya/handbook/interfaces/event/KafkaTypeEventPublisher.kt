package dev.sayaya.handbook.interfaces.event

import com.fasterxml.jackson.databind.ObjectMapper
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.domain.event.Event
import dev.sayaya.handbook.domain.event.TypeEvent
import dev.sayaya.handbook.usecase.TypeEventPublisher
import org.springframework.kafka.core.KafkaTemplate
import java.util.*

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
