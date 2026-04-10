package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.DocumentPatch
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * 문서 CRUD 비즈니스 로직.
 * Spring 어노테이션 없음 — interfaces.config에서 Bean 등록.
 */
class DocumentService(
    private val documentRepository: DocumentRepository,
    private val eventPublisher: DocumentEventPublisher,
) {
    fun save(workspace: UUID, documents: List<Document>): Flux<Document> {
        return documentRepository.saveAll(workspace, documents)
            .doOnNext { document -> eventPublisher.publishCreated(workspace, document) }
    }

    fun patch(workspace: UUID, patches: List<DocumentPatch>): Flux<Document> {
        return documentRepository.patchAll(workspace, patches)
            .doOnNext { document -> eventPublisher.publishCreated(workspace, document) }
    }

    fun delete(workspace: UUID, documents: List<Document>): Mono<Void> {
        return documentRepository.deleteAll(workspace, documents)
            .doOnSuccess { documents.forEach { document -> eventPublisher.publishDeleted(workspace, document) } }
    }
}
