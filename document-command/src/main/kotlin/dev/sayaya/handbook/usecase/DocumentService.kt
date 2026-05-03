package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.DocumentPatch
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * 문서 CUD 비즈니스 로직 (유스케이스 계층).
 *
 * **책임:** save(전체 저장), patch(부분 업데이트), delete 실행 후 Kafka 이벤트 발행.
 *
 * **의존관계:**
 * - [DocumentRepository] — 영속화 포트 (R2DBC 어댑터가 구현)
 * - [DocumentEventPublisher] — Kafka 이벤트 발행 (DOCUMENT_CREATED/DELETED)
 *
 * **주의:** Spring 어노테이션 없음 — interfaces.config.DocumentConfig에서 Bean 등록.
 * patch() 성공 시에도 DOCUMENT_CREATED 이벤트를 발행한다 (변경을 다른 사용자에게 알림).
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

    fun findAll(workspace: UUID, type: String?): Flux<Document> {
        return documentRepository.findAll(workspace, type)
    }

    /**
     * 문서의 워크플로우 상태를 전이한다.
     *
     * **허용되는 전이:**
     * - DRAFT → REVIEW
     * - REVIEW → PUBLISHED
     * - REVIEW → DRAFT
     * - PUBLISHED → DRAFT
     *
     * @param workspace 워크스페이스 ID
     * @param documentId 대상 문서 ID
     * @param newStatus 전이할 상태 (DRAFT, REVIEW, PUBLISHED)
     * @param userId 상태 변경 요청자
     * @return 상태가 변경된 문서
     * @throws IllegalStateException 허용되지 않는 전이일 때
     */
    fun updateStatus(workspace: UUID, documentId: UUID, newStatus: String, userId: String): Mono<Document> {
        val allowedTransitions = mapOf(
            "DRAFT" to setOf("REVIEW"),
            "REVIEW" to setOf("PUBLISHED", "DRAFT"),
            "PUBLISHED" to setOf("DRAFT"),
        )
        return documentRepository.findById(documentId)
            .switchIfEmpty(Mono.error(IllegalArgumentException("Document not found: $documentId")))
            .flatMap { document ->
                val allowed = allowedTransitions[document.status()] ?: emptySet()
                if (newStatus !in allowed) {
                    Mono.error(IllegalStateException("Invalid status transition: ${document.status()} → $newStatus"))
                } else {
                    documentRepository.updateStatus(documentId, newStatus)
                        .doOnNext { updated -> eventPublisher.publishCreated(workspace, updated) }
                }
            }
    }

    fun delete(workspace: UUID, documents: List<Document>): Mono<Void> {
        return documentRepository.deleteAll(workspace, documents)
            .doOnSuccess { documents.forEach { document -> eventPublisher.publishDeleted(workspace, document) } }
    }
}
