package dev.sayaya.handbook.`interface`.database

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.f4b6a3.ulid.Ulid
import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.usecase.DocumentRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@Repository
class R2dbcDocumentRepository (
    override val databaseClient: DatabaseClient,
    val objectMapper: ObjectMapper
): DocumentRepository, R2dbcDocumentBatchUpsertRepository, R2dbcDocumentBatchDeleteRepository {
    override val log: Logger = LoggerFactory.getLogger(this::class.java)
    override fun saveAll(workspace: UUID, documents: List<Document>): Mono<List<Document>> = getAuthenticatedUser().flatMap { user ->
        val documentsAndEntities = documents.toEntity(workspace)
        val entities = documentsAndEntities.map { it.second.copy(id = Ulid.fast().toUuid()) }
        saveAll(entities, user, Instant.now()).collectList()
            .map { savedEntities ->
                val savedEntitiesMap = savedEntities.associateBy { it.id }
                documentsAndEntities.mapNotNull { originalPair ->
                    val originalDocument = originalPair.first
                    val entityPreparedForSave = originalPair.second
                    val savedEntity = savedEntitiesMap[entityPreparedForSave.id]
                    if (savedEntity != null) originalDocument.copy (
                        id = savedEntity.id,
                        createDateTime = savedEntity.createDateTime,
                        creator = user
                    ) else {
                        log.warn("Document with pre-save ID ${entityPreparedForSave.id} was not found in the saved entities list. It might have failed to save or was not returned by the batch save operation.")
                        null
                    }
                }
            }
    }.doOnError { error ->
        log.error("Batch execution failed inside inConnection!", error)
    }

    override fun deleteAll(workspace: UUID, documents: List<Document>): Mono<List<Document>> = deleteAll(documents.toEntity(workspace).map { it.second }).
    collectList().doOnError { error ->
        log.error("Batch execution failed inside inConnection!", error)
    }.thenReturn(documents)

    private fun getAuthenticatedUser(): Mono<String> = ReactiveSecurityContextHolder.getContext().map { it.authentication.name }
        .switchIfEmpty(Mono.error(IllegalStateException("Unable to retrieve authenticated user context")))

    private fun List<Document>.toEntity(workspace: UUID) = map { document ->
        document to R2dbcDocumentEntity.of (
            workspace = workspace,
            id = document.id ?: Ulid.fast().toUuid(),
            type = document.type,
            serial = document.serial,
            effectDateTime = document.effectDateTime, expireDateTime =  document.expireDateTime,
            data = objectMapper.writeValueAsString(document.data)
        )
    }
}