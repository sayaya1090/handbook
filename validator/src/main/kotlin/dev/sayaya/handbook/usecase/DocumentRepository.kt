package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.UUID

interface DocumentRepository {
    fun findByType(workspace: UUID, type: String, effectDateTime: Instant, expireDateTime: Instant): Flux<Document>
    fun findById(workspace: UUID, id: UUID): Mono<Document>
}