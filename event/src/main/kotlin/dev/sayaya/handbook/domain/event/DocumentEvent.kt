package dev.sayaya.handbook.domain.event

import dev.sayaya.handbook.domain.Document
import java.util.*

/**
 * 문서 도메인에서 발생하는 이벤트.
 * 허용되는 이벤트 타입: DOCUMENT_CREATED, DOCUMENT_DELETED
 */
data class DocumentEvent(
    override val id: UUID,
    override val workspace: UUID,
    override val eventType: Event.EventType,
    override val payload: Document,
) : Event<Document> {
    init {
        require(eventType in ALLOWED_TYPES) {
            "Invalid event type for DocumentEvent: $eventType. Allowed: $ALLOWED_TYPES"
        }
    }
    companion object {
        private val ALLOWED_TYPES = setOf(
            Event.EventType.DOCUMENT_CREATED,
            Event.EventType.DOCUMENT_DELETED,
        )
    }
}
