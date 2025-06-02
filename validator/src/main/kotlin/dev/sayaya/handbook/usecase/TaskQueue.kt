package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import reactor.core.publisher.Mono
import java.util.UUID

interface TaskQueue {
    fun publish(workspace: UUID, document: Document): Mono<Void>
}