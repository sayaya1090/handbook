package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Type
import java.util.*

/**
 * 타입 이벤트 발행 포트.
 *
 * **책임:** 타입 생성/삭제 시 Kafka로 [TYPE_CREATED][dev.sayaya.handbook.domain.event.Event.EventType.TYPE_CREATED] /
 * [TYPE_DELETED][dev.sayaya.handbook.domain.event.Event.EventType.TYPE_DELETED] 이벤트를 발행한다.
 *
 * **의존관계:**
 * - [KafkaTypeEventPublisher][dev.sayaya.handbook.interfaces.event.KafkaTypeEventPublisher] — Kafka 어댑터 구현체
 */
interface TypeEventPublisher {
    fun publishCreated(workspace: UUID, type: Type)
    fun publishDeleted(workspace: UUID, type: Type)
}
