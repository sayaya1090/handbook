package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import java.util.*

/** 문서 이벤트 발행 포트. Kafka로 DOCUMENT_CREATED/DOCUMENT_DELETED 이벤트를 발행한다. */
interface DocumentEventPublisher {
    fun publishCreated(workspace: UUID, document: Document)
    fun publishDeleted(workspace: UUID, document: Document)
}
