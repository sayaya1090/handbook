package dev.sayaya.handbook.domain.event

import java.io.Serializable
import java.util.*

/**
 * 사용자 프레즌스 이벤트.
 * 사용자가 편집 중인 위치(타입/문서/필드)를 다른 사용자에게 알린다.
 * DB 저장 없이 Kafka → SSE로 즉시 전달된다.
 */
data class PresenceEvent(
    override val id: UUID = UUID.randomUUID(),
    override val workspace: UUID,
    override val eventType: Event.EventType = Event.EventType.PRESENCE,
    override val payload: PresencePayload,
) : Event<PresencePayload>

data class PresencePayload(
    /** 프레즌스를 보내는 사용자 ID */
    val user: String,
    /** 사용자 표시 이름 */
    val userName: String? = null,
    /** 편집 중인 타입 (null이면 프레즌스 해제) */
    val type: String? = null,
    /** 편집 중인 문서 serial */
    val serial: String? = null,
    /** 편집 중인 필드명 */
    val field: String? = null,
) : Serializable
