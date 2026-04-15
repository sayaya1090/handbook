package dev.sayaya.handbook.interfaces.event

import tools.jackson.databind.ObjectMapper
import dev.sayaya.handbook.domain.AgentCommand
import dev.sayaya.handbook.domain.event.AgentCommandEvent
import dev.sayaya.handbook.domain.event.Event
import dev.sayaya.handbook.usecase.AgentCommandEventPublisher
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaTemplate
import java.util.UUID

class KafkaAgentCommandEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
) : AgentCommandEventPublisher {

    override fun publish(workspace: UUID, seq: Int, command: AgentCommand) {
        val event = AgentCommandEvent(
            id = UUID.randomUUID(),
            workspace = workspace,
            payload = AgentCommandEvent.AgentCommandPayload(
                seq = seq,
                type = command.type.name.lowercase(),
                target = command.payload,
                description = command.target,
            ),
        )
        val json = objectMapper.writeValueAsString(event)
        val record = ProducerRecord<String, String>(TOPIC, workspace.toString(), json)
        kafkaTemplate.send(record)
    }

    companion object {
        const val TOPIC = "handbook-events"
    }
}
