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
    override fun saveAll(workspace: UUID, documents: List<Document>): Mono<List<Document>> = getAuthenticatedUser().flatMapMany { user ->
        saveAll(documents.toEntity(workspace), user, Instant.now())
    }.collectList().doOnError { error ->
        log.error("Batch execution failed inside inConnection!", error)
    }.thenReturn(documents)

    override fun deleteAll(workspace: UUID, documents: List<Document>): Mono<List<Document>> = deleteAll(documents.toEntity(workspace)).
    collectList().doOnError { error ->
        log.error("Batch execution failed inside inConnection!", error)
    }.thenReturn(documents)

    private fun getAuthenticatedUser(): Mono<String> = ReactiveSecurityContextHolder.getContext().map { it.authentication.name }
        .switchIfEmpty(Mono.error(IllegalStateException("Unable to retrieve authenticated user context")))

    private fun List<Document>.toEntity(workspace: UUID) = map { document ->
        R2dbcDocumentEntity.of (
            workspace = workspace,
            id = Ulid.fast().toUuid(),
            type = document.type,
            serial = document.serial,
            effectDateTime = document.effectDateTime, expireDateTime =  document.expireDateTime,
            data = objectMapper.writeValueAsString(document.data)
        )
    }
}