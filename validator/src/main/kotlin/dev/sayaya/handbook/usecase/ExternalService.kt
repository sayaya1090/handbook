package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import reactor.core.publisher.Mono
import java.util.*

interface ExternalService {
    fun publish(workspace: UUID, document: Document): Mono<Void>
}