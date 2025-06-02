package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import reactor.core.publisher.Flux
import java.time.Instant
import java.util.UUID

interface DocumentRepository {
    fun findByType(workspace: UUID, type: String, effectDateTime: Instant, expireDateTime: Instant): Flux<Document>
}