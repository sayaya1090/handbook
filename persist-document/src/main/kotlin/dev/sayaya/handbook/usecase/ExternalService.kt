package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import reactor.core.publisher.Mono
import java.security.Principal
import java.util.*

interface ExternalService {
    fun publish(principal: Principal, workspace: UUID, types: Map<DocumentKey, Document?>): Mono<Void>
    data class DocumentKey (
        val serial: String,
        val type: String
    ) {
        companion object {
            fun of(document: Document): DocumentKey = DocumentKey(document.serial, document.type)
        }
    }
}