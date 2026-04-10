package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import java.util.*

/**
 * 문서 이벤트 발행 포트.
 *
 * **책임:** 문서 생성/삭제 시 Kafka로 [DOCUMENT_CREATED][dev.sayaya.handbook.domain.event.Event.EventType.DOCUMENT_CREATED] /
 * [DOCUMENT_DELETED][dev.sayaya.handbook.domain.event.Event.EventType.DOCUMENT_DELETED] 이벤트를 발행한다.
 *
 * **의존관계:**
 * - [KafkaDocumentEventPublisher][dev.sayaya.handbook.interfaces.event.KafkaDocumentEventPublisher] — Kafka 어댑터 구현체
 */
interface DocumentEventPublisher {
    fun publishCreated(workspace: UUID, document: Document)
    fun publishDeleted(workspace: UUID, document: Document)
}
