package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Type
import java.util.*

/** 타입 이벤트 발행 포트. Kafka로 TYPE_CREATED/TYPE_DELETED 이벤트를 발행한다. */
interface TypeEventPublisher {
    fun publishCreated(workspace: UUID, type: Type)
    fun publishDeleted(workspace: UUID, type: Type)
}
