package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.`interface`.database.R2dbcDocumentEntity.Companion.R2dbcDocumentId
import org.slf4j.Logger
import org.springframework.r2dbc.core.DatabaseClient
import reactor.core.publisher.Flux

interface R2dbcDocumentBatchDeleteRepository {
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
}