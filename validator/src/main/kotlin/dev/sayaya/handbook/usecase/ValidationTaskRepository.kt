package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import reactor.core.publisher.Mono

interface ValidationTaskRepository {
    fun expire(documents: List<Document>): Mono<Void>
}