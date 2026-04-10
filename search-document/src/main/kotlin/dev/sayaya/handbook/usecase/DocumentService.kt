package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.Search
import org.springframework.data.domain.Page
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

/**
 * 문서 읽기 전용 비즈니스 로직.
 * Spring 어노테이션 없음 — interfaces.config에서 Bean 등록.
 */
class DocumentService(private val repo: DocumentRepository) {
    fun search(workspace: UUID, param: Search): Mono<Page<Document>> = repo.search(workspace, param)
    fun find(workspace: UUID, type: String, serial: String, date: Instant?): Mono<Document> {
        val effectiveDate = date ?: Instant.now()
        return repo.find(workspace, type, serial, effectiveDate)
    }
}
