package dev.sayaya.handbook.domain.event

import dev.sayaya.handbook.domain.Type
import java.util.UUID

data class TypeEvent (
    override val id: UUID,
    override val workspace: UUID,
    override val param: Type,
    override val type: Event.EventType
): Event<Type, TypeEvent> {
    init {
        require(type in ALLOWED_TYPE_EVENT_TYPES) {
            "Invalid event type for TypeEvent: $type. Allowed types are: $ALLOWED_TYPE_EVENT_TYPES"
        }
    }
    companion object {
        private val ALLOWED_TYPE_EVENT_TYPES = setOf(
            Event.EventType.CREATE_TYPE,
            Event.EventType.UPDATE_TYPE,
            Event.EventType.DELETE_TYPE
        )
    }
}