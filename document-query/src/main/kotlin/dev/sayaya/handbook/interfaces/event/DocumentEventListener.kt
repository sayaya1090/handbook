package dev.sayaya.handbook.interfaces.event

import com.fasterxml.jackson.databind.ObjectMapper
import dev.sayaya.handbook.domain.event.DocumentEvent
import dev.sayaya.handbook.domain.event.Event
import dev.sayaya.handbook.interfaces.database.ElasticsearchDocumentEntity
import dev.sayaya.handbook.interfaces.database.ElasticsearchDocumentRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.UUID

/**
 * 문서 이벤트 컨슈머.
 *
 * **역할:** 'handbook-events' 토픽에서 문서 이벤트를 수신하여 Elasticsearch 에 동기화한다.
 */
@Component
class DocumentEventListener(
    private val repository: ElasticsearchDocumentRepository,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["handbook-events"], groupId = "document-query-es-sync")
    fun onEvent(message: String) {
        try {
            val event = objectMapper.readValue(message, Event::class.java)
            if (event is DocumentEvent) {
                sync(event).subscribe()
            }
        } catch (e: Exception) {
            logger.error("Failed to sync document event to Elasticsearch: {}", e.message, e)
        }
    }

    private fun sync(event: DocumentEvent): Mono<Void> {
        val workspace = event.workspace
        val doc = event.payload
        return when (event.eventType) {
            Event.EventType.DOCUMENT_CREATED -> {
                logger.info("Syncing created/updated document: {}/{}", workspace, doc.id())
                repository.save(ElasticsearchDocumentEntity.fromDomain(workspace, doc)).then()
            }
            Event.EventType.DOCUMENT_DELETED -> {
                logger.info("Syncing deleted document: {}/{}", workspace, doc.id())
                repository.deleteByWorkspaceAndId(workspace, UUID.fromString(doc.id()))
            }
            else -> Mono.empty()
        }
    }
}
