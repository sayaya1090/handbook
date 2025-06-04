package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import reactor.core.publisher.Mono
import java.security.Principal
import java.util.*

interface ExternalService {
    // Map의 값이 null인 경우는 삭제를 의미
    fun publish(principal: Principal, workspace: UUID, documents: Map<DocumentKey, Document?>): Mono<Void>
    data class DocumentKey (
        val serial: String,
        val type: String
    ) {
        companion object {
            fun of(document: Document): DocumentKey = DocumentKey(document.serial, document.type)
        }
    }
}