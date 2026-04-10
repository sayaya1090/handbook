package dev.sayaya.handbook.interfaces.database

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.usecase.DocumentRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

interface R2dbcDocumentEntityRepository : ReactiveCrudRepository<R2dbcDocumentEntity, UUID>

class R2dbcDocumentRepositoryAdapter(
    private val repo: R2dbcDocumentEntityRepository,
    private val objectMapper: ObjectMapper,
    private val tx: TransactionalOperator,
) : DocumentRepository {

    override fun saveAll(workspace: UUID, documents: List<Document>): Flux<Document> {
        return Flux.fromIterable(documents)
            .map { doc ->
                val serializedData = objectMapper.writeValueAsString(doc.data)
                R2dbcDocumentEntity.fromDomain(workspace, doc, serializedData)
            }
            .collectList()
            .flatMapMany { entities -> repo.saveAll(entities) }
            .map { entity -> entity.toDomainWithData(objectMapper) }
            .`as`(tx::transactional)
    }

    override fun deleteAll(workspace: UUID, documents: List<Document>): Mono<Void> {
        val ids = documents.mapNotNull { it.id }
        return repo.deleteAllById(ids)
            .`as`(tx::transactional)
    }

    private fun R2dbcDocumentEntity.toDomainWithData(objectMapper: ObjectMapper): Document {
        val dataMap: Map<String, String?> = objectMapper.readValue(data)
        return Document(
            id = id,
            type = type,
            serial = serial,
            effectDateTime = effectDateTime,
            expireDateTime = expireDateTime,
            createDateTime = createDateTime,
            creator = creator,
            data = dataMap,
        )
    }
}
