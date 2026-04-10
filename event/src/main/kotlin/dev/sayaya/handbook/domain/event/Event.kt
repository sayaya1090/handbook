package dev.sayaya.handbook.domain.event

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.io.Serializable
import java.util.*

/**
 * 도메인 변경 시 발행되는 이벤트를 나타내는 인터페이스.
 *
 * 불변 이력 모델에 따라, 변경은 항상 "새 버전 생성" 또는 "삭제"로 표현되므로
 * UPDATE 이벤트는 존재하지 않는다.
 *
 * @param T 이벤트 페이로드 타입
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "event_type", visible = true)
@JsonSubTypes(
    JsonSubTypes.Type(value = DocumentEvent::class, name = "DOCUMENT_CREATED"),
    JsonSubTypes.Type(value = DocumentEvent::class, name = "DOCUMENT_DELETED"),
    JsonSubTypes.Type(value = TypeEvent::class, name = "TYPE_CREATED"),
    JsonSubTypes.Type(value = TypeEvent::class, name = "TYPE_DELETED"),
    JsonSubTypes.Type(value = ValidationEvent::class, name = "VALIDATION_REQUESTED"),
    JsonSubTypes.Type(value = AgentCommandEvent::class, name = "AGENT_COMMAND"),
)
@JsonInclude(JsonInclude.Include.NON_NULL)
interface Event<T : Serializable> : Serializable {
    val id: UUID
    val workspace: UUID
    val eventType: EventType
    val payload: T

    enum class EventType {
        DOCUMENT_CREATED,
        DOCUMENT_DELETED,
        TYPE_CREATED,
        TYPE_DELETED,
        VALIDATION_REQUESTED,
        AGENT_COMMAND,
    }
}
