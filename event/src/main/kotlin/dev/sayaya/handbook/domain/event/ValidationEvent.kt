package dev.sayaya.handbook.domain.event

import java.util.*

/**
 * 검증 요청 이벤트.
 * 타입 새 버전 생성 또는 문서 생성 시 발행되어 비동기 검증을 트리거한다.
 *
 * @property payload 검증 대상 식별 정보 (타입 id 또는 문서 id 등)
 */
data class ValidationEvent(
    override val id: UUID,
    override val workspace: UUID,
    override val eventType: Event.EventType,
    override val payload: ValidationPayload,
) : Event<ValidationPayload> {
    init {
        require(eventType == Event.EventType.VALIDATION_REQUESTED) {
            "Invalid event type for ValidationEvent: $eventType. Must be VALIDATION_REQUESTED"
        }
    }
}
