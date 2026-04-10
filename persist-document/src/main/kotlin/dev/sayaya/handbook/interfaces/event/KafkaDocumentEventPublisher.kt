package dev.sayaya.handbook.interfaces.event

import com.fasterxml.jackson.databind.ObjectMapper
import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.event.DocumentEvent
import dev.sayaya.handbook.domain.event.Event
import dev.sayaya.handbook.usecase.DocumentEventPublisher
import org.springframework.kafka.core.KafkaTemplate
import java.util.*

class KafkaDocumentEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    private val topic: String = "handbook-events",
) : DocumentEventPublisher {

    override fun publishCreated(workspace: UUID, document: Document) {
        val event = DocumentEvent(
            id = UUID.randomUUID(),
            workspace = workspace,
            eventType = Event.EventType.DOCUMENT_CREATED,
            payload = document,
        )
        kafkaTemplate.send(topic, workspace.toString(), objectMapper.writeValueAsString(event))
    }

    override fun publishDeleted(workspace: UUID, document: Document) {
        val event = DocumentEvent(
            id = UUID.randomUUID(),
            workspace = workspace,
            eventType = Event.EventType.DOCUMENT_DELETED,
            payload = document,
        )
        kafkaTemplate.send(topic, workspace.toString(), objectMapper.writeValueAsString(event))
    }
}
