package dev.sayaya.handbook.domain.event

import dev.sayaya.handbook.domain.Type
import java.util.*

/**
 * 타입 도메인에서 발생하는 이벤트.
 * 허용되는 이벤트 타입: TYPE_CREATED, TYPE_DELETED
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
