package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.usecase.ValidationTaskRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
class R2dbcValidationTaskRepository (
    private val template: R2dbcEntityTemplate,
): ValidationTaskRepository {
    private val databaseClient = template.databaseClient
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
    override fun expire(workspace: UUID, documents: List<Document>): Mono<Void> {
        val values = buildString {
            documents.joinTo(this, separator = ",") { "(${it.toCsv(workspace)})" }
        }
        val sql = "$EXPIRE_TASKS_SQL IN ($values)"
        log.info(sql)
        return databaseClient.sql(sql).fetch().rowsUpdated().then()
    }
    companion object {
        private const val EXPIRE_TASKS_SQL = """
            UPDATE validation_task SET last=FALSE WHERE (workspace, document, last)
        """
        private fun escapeSql(value: String?): String {
            if (value == null) return "NULL" // SQL의 NULL 값으로 처리
            val escapedValue = value.replace("'", "''")
            return "'$escapedValue'"
        }
        private fun Document.toCsv(workspace: UUID): String = """
            ${escapeSql(workspace.toString())},
            ${escapeSql(id.toString())},
            TRUE
        """.trimIndent()
    }
}

/*
val databaseClient: DatabaseClient
    val log: Logger
    fun deleteAll(entities: List<R2dbcDocumentEntity>): Flux<R2dbcDocumentEntity> {
        val values = buildString {
            entities.joinTo(this, separator = ",") { "(${it.pk.toCsv()})" }
        }
        val sql = "$DELETE_TYPE_SQL IN ($values)"
        log.info(sql)
        return databaseClient.sql(sql).fetch().rowsUpdated().thenMany(Flux.fromIterable(entities))
    }
    companion object {
        private const val DELETE_TYPE_SQL = """
            UPDATE document SET last=FALSE WHERE (workspace, id, last)
        """
        private fun escapeSql(value: String?): String {
            if (value == null) return "NULL" // SQL의 NULL 값으로 처리
            val escapedValue = value.replace("'", "''")
            return "'$escapedValue'"
        }
        private fun R2dbcDocumentId.toCsv(): String = """
            ${escapeSql(workspace.toString())},
            ${escapeSql(id.toString())},
            TRUE
        """.trimIndent()
    }
 */