package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.Search
import org.springframework.data.domain.Page
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.UUID

interface DocumentRepository: Searchable<Document>  {
    fun search(workspace: UUID, param: Search): Mono<Page<Document>>
    fun find(workspace: UUID, type: String, serial: String, date: Instant): Mono<Document>
}