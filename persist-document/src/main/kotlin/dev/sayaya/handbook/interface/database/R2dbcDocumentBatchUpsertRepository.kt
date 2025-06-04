package dev.sayaya.handbook.`interface`.database

import org.slf4j.Logger
import org.springframework.r2dbc.core.DatabaseClient
import reactor.core.publisher.Flux
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

interface R2dbcDocumentBatchUpsertRepository {
    val databaseClient: DatabaseClient
    val log: Logger
    fun saveAll(entities: List<R2dbcDocumentEntity>, createdBy: String, createdAt: Instant): Flux<R2dbcDocumentEntity> {
        val constantCsv = listOf(escapeSql(createdBy), formatTimestampLiteral(createdAt)).joinToString(separator = ", ", prefix = ", ")
        val values = buildString {
            entities.joinTo(this, separator = ",") { "(${it.toCsv()}$constantCsv)" }
        }
        val sql = "$INSERT_DOCUMENT_SQL VALUES $values"
        log.info(sql)
        return databaseClient.sql(sql).fetch().rowsUpdated().thenMany(Flux.fromIterable(entities)).map { it.apply {
            createBy = UUID.fromString(createdBy)
            createDateTime = createdAt
        } }
    }
    companion object {
        private const val INSERT_DOCUMENT_SQL = """
            INSERT INTO document (workspace, id, type, serial, effective_at, expire_at, data, created_by, created_at) 
        """
        private val timestampFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSXXX").withZone(ZoneOffset.UTC)

        // toCsv에서 문자열 삽입 시 이 함수를 사용하여 escape 하여 injection 방지
        private fun escapeSql(value: String?): String {
            if (value == null) return "NULL" // SQL의 NULL 값으로 처리
            val escapedValue = value.replace("'", "''")
            return "'$escapedValue'"
        }
        private fun formatTimestampLiteral(instant: Instant?): String = if (instant == null) "NULL"
        else "'${timestampFormatter.format(instant)}'"
        fun R2dbcDocumentEntity.toCsv(): String = """
            ${escapeSql(workspace.toString())},
            ${escapeSql(id.toString())},
            ${escapeSql(type)},
            ${escapeSql(serial)},
            ${formatTimestampLiteral(effectDateTime)},
            ${formatTimestampLiteral(expireDateTime)},
            ${escapeSql(data)}
        """
    }
}