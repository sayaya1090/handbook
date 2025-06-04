package dev.sayaya.handbook.domain.event

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.io.Serializable
import java.util.UUID

@JsonTypeInfo(use = NAME, property = "type", visible = true)
@JsonSubTypes(
    JsonSubTypes.Type(value = DocumentEvent::class, name = "CREATE_DOCUMENT"),
    JsonSubTypes.Type(value = DocumentEvent::class, name = "UPDATE_DOCUMENT"),
    JsonSubTypes.Type(value = DocumentEvent::class, name = "DELETE_DOCUMENT"),

    JsonSubTypes.Type(value = TypeEvent::class, name = "CREATE_TYPE"),
    JsonSubTypes.Type(value = TypeEvent::class, name = "UPDATE_TYPE"),
    JsonSubTypes.Type(value = TypeEvent::class, name = "DELETE_TYPE"),
) @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
interface Event<T: Serializable, N: Event<T, N>>: Serializable {
    val id: UUID
    val workspace: UUID
    val type: EventType
    val param: T
    enum class EventType {
        CREATE_DOCUMENT,
        UPDATE_DOCUMENT,
        DELETE_DOCUMENT,

        CREATE_TYPE,
        UPDATE_TYPE,
        DELETE_TYPE,
    }
}