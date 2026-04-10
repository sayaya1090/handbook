package dev.sayaya.handbook.domain.event

import com.fasterxml.jackson.annotation.JsonProperty
import dev.sayaya.handbook.domain.Document
import java.util.*

/**
 * 문서 도메인에서 발생하는 이벤트.
 *
 * **책임:** 문서 생성/삭제 시 [Document][dev.sayaya.handbook.domain.Document] 페이로드와 함께 발행된다.
 * persist-document에서 Kafka로 발행되고, event-broadcaster를 통해 SSE로 클라이언트에 전달된다.
 *
 * **주의:** 허용되는 이벤트 타입은 [DOCUMENT_CREATED][Event.EventType.DOCUMENT_CREATED],
 * [DOCUMENT_DELETED][Event.EventType.DOCUMENT_DELETED]뿐이며, 그 외 타입은 init 블록에서 예외가 발생한다.
 */
data class DocumentEvent(
    override val id: UUID,
    override val workspace: UUID,
    @JsonProperty("event_type") override val eventType: Event.EventType,
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
