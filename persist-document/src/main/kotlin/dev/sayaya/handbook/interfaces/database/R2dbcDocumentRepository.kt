package dev.sayaya.handbook.interfaces.database

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.DocumentPatch
import dev.sayaya.handbook.usecase.DocumentRepository
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

interface R2dbcDocumentEntityRepository : ReactiveCrudRepository<R2dbcDocumentEntity, UUID>

class R2dbcDocumentRepositoryAdapter(
    private val repo: R2dbcDocumentEntityRepository,
    private val objectMapper: ObjectMapper,
    private val tx: TransactionalOperator,
    private val databaseClient: DatabaseClient,
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

    override fun patchAll(workspace: UUID, patches: List<DocumentPatch>): Flux<Document> {
        return Flux.fromIterable(patches)
            .flatMapSequential { patch -> patchOne(patch) }
            .`as`(tx::transactional)
    }

    private fun patchOne(patch: DocumentPatch): Mono<Document> {
        val patchJson = objectMapper.writeValueAsString(patch.data)
        return databaseClient.sql("""
            UPDATE documents
            SET data = data || :patchData::jsonb, rev = rev + 1
            WHERE id = :id AND rev = :rev
        """.trimIndent())
            .bind("patchData", patchJson)
            .bind("id", patch.id)
            .bind("rev", patch.rev)
            .fetch().rowsUpdated()
            .flatMap { rowsUpdated ->
                if (rowsUpdated == 0L) Mono.error(DuplicateKeyException("Version conflict for document ${patch.id}"))
                else repo.findById(patch.id).map { it.toDomainWithData(objectMapper) }
            }
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
