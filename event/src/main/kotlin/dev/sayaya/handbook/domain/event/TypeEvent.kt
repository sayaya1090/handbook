package dev.sayaya.handbook.domain.event

import dev.sayaya.handbook.domain.Type
import java.util.*

/**
 * 타입 도메인에서 발생하는 이벤트.
 *
 * **책임:** 타입 생성/삭제 시 [Type][dev.sayaya.handbook.domain.Type] 페이로드와 함께 발행된다.
 * persist-type에서 Kafka로 발행되고, event-broadcaster를 통해 SSE로 클라이언트에 전달된다.
 *
 * **주의:** 허용되는 이벤트 타입은 [TYPE_CREATED][Event.EventType.TYPE_CREATED],
 * [TYPE_DELETED][Event.EventType.TYPE_DELETED]뿐이며, 그 외 타입은 init 블록에서 예외가 발생한다.
 */
data class TypeEvent(
    override val id: UUID,
    override val workspace: UUID,
    override val eventType: Event.EventType,
    override val payload: Type,
) : Event<Type> {
    init {
        require(eventType in ALLOWED_TYPES) {
            "Invalid event type for TypeEvent: $eventType. Allowed: $ALLOWED_TYPES"
        }
    }
    companion object {
        private val ALLOWED_TYPES = setOf(
            Event.EventType.TYPE_CREATED,
            Event.EventType.TYPE_DELETED,
        )
    }
}
