package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import reactor.core.publisher.Mono
import java.util.UUID

interface ValidationTaskRepository {
    fun expire(workspace: UUID, documents: List<Document>): Mono<Void>
    fun save(workspace: UUID, document: Document, result: Map<String, Boolean>): Mono<Void>
}