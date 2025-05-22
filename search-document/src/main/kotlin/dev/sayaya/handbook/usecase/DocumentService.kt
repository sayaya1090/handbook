package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.Search
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class DocumentService(private val repo: DocumentRepository) {
    fun search(workspace: UUID, param: Search): Mono<Page<Document>> = param.copy (
        filters = param.filters + ("workspace" to workspace)
    ).let(repo::search)
}