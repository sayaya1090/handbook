package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.usecase.DocumentRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.*

@Repository
class R2dbcDocumentRepository: DocumentRepository {
    override fun saveAll(workspace: UUID, documents: List<Document>): Mono<List<Document>> {
        TODO("Not yet implemented")
    }
}