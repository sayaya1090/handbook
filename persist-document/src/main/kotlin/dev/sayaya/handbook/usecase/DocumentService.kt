package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.security.Principal
import java.util.UUID

@Service
class DocumentService(
    private val repo: DocumentRepository,
    private val eventHandler: ExternalServiceHandler,
) {
    fun save(principal: Principal, workspace: UUID, documents: List<Document>): Mono<List<Document>> = if (documents.isEmpty()) Mono.empty()
    else repo.saveAll(workspace, documents).delayUntil {
        eventHandler.publish(principal, workspace, it.associateBy(ExternalService.DocumentKey::of))
    }
    fun delete(principal: Principal, workspace: UUID, documents: List<Document>): Mono<List<Document>> = if (documents.isEmpty()) Mono.empty()
    else repo.deleteAll(workspace, documents).delayUntil {
        eventHandler.publish(principal, workspace, it.associateBy(ExternalService.DocumentKey::of))
    }
}