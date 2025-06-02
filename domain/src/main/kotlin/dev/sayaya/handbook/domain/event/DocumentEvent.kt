package dev.sayaya.handbook.domain.event

import dev.sayaya.handbook.domain.Document
import java.util.UUID

data class DocumentEvent (
    override val id: UUID,
    override val workspace: UUID,
    override val param: Document,
    override val type: Event.EventType
): Event<Document, DocumentEvent> {
    init {
        require(type in ALLOWED_DOCUMENT_EVENT_TYPES) {
            "Invalid event type for DocumentEvent: $type. Allowed types are: $ALLOWED_DOCUMENT_EVENT_TYPES"
        }
    }
    companion object {
        private val ALLOWED_DOCUMENT_EVENT_TYPES = setOf(
            Event.EventType.CREATE_DOCUMENT,
            Event.EventType.UPDATE_DOCUMENT,
            Event.EventType.DELETE_DOCUMENT
        )
    }
}