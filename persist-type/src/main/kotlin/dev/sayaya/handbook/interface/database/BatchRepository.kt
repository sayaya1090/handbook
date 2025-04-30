package dev.sayaya.handbook.`interface`.database

import org.springframework.r2dbc.core.DatabaseClient
import reactor.core.publisher.Flux
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

interface BatchRepository<T> {
    val databaseClient: DatabaseClient
    val timestampFormatter: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS").withZone(ZoneOffset.UTC)

    // toCsv에서 문자열 삽입 시 이 함수를 사용하여 escape 하여 injection 방지
    fun escapeSql(value: String?): String {
        if (value == null) return "NULL" // SQL의 NULL 값으로 처리
        val escapedValue = value.replace("'", "''")
        return "'$escapedValue'"

    }
    fun formatTimestampLiteral(instant: Instant?): String = if (instant == null) "NULL"
    else "'${timestampFormatter.format(instant)}'"

    fun insertInto(): String
    fun condition(): String = ""
    fun T.toCsv(): String
    fun saveAll(entities: List<T>, vararg constant: String): Flux<T> {
        val constantCsv = if (constant.isNotEmpty()) {
            constant.joinToString(separator = ", ", prefix = ", ")
        } else ""
        val values = buildString {
            entities.joinTo(this, separator = ",") { "(${it.toCsv()}$constantCsv)" }
        }
        val sql = "${insertInto()} VALUES $values ${condition()}"
        return databaseClient.sql(sql).fetch().rowsUpdated().thenMany(Flux.fromIterable(entities))
    }
}