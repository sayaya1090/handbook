package dev.sayaya.handbook.domain.event

import java.util.*

/**
 * 검증 요청 이벤트.
 *
 * **책임:** 타입 새 버전 생성 또는 문서 생성 시 발행되어 비동기 검증을 트리거한다.
 * 검증 서비스가 이 이벤트를 구독하여 타입/문서의 유효성을 검사한다.
 *
 * **주의:** eventType은 반드시 [VALIDATION_REQUESTED][Event.EventType.VALIDATION_REQUESTED]여야 하며,
 * 그 외 타입은 init 블록에서 예외가 발생한다.
 *
 * @property payload 검증 대상 식별 정보 ([ValidationPayload] — 타입 ID, 버전, 문서 ID)
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
