package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.Search
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.UUID

@Service
class DocumentService(private val repo: DocumentRepository) {
    fun search(workspace: UUID, param: Search): Mono<Page<Document>> = repo.search(workspace, param)
    fun find(workspace: UUID, type: String, serial: String, _date: Instant?): Mono<Map<String, String?>> {
        val date = _date ?: Instant.now()
        return repo.find(workspace, type, serial, date).map { document ->
            document.data
        }
    }
}