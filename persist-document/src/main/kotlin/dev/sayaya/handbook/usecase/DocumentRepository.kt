package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import reactor.core.publisher.Mono
import java.util.UUID

interface DocumentRepository {
    fun saveAll(workspace: UUID, documents: List<Document>): Mono<List<Document>>
    fun deleteAll(workspace: UUID, documents: List<Document>): Mono<List<Document>>
}