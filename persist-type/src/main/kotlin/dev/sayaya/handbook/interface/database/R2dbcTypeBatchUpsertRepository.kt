package dev.sayaya.handbook.`interface`.database

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.r2dbc.core.DatabaseClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

/*
 * Type 엔티티는 불변으로 설계되었다. 변경 시 기존 타입을 새로 추가하면,
 * 트리거에 의해 기존 last=true는 false로 변경된다.
 * 따라서 Upsert 는 Insert만 수행하도록 처리한다.
 *
 * 여러 데이터를 한번에 저장하려면
 * INSERT INTO ... VALUES (), (), () 구문을 사용하여 Batch 처리를 하는 것이
 * 효율적인데, R2DBC는 아직 EntityTemplate를 사용한 Batch 처리를 지원하지 않는다.
 * (Parameterized State의 add()를 사용하여 Batch처리를 요청하더라도 내부적으로 여러
 * Insert 쿼리를 생성하여 처리한다.)
 * 따라서 R2DBC의 DatabaseClient를 사용하여 직접 쿼리를 작성하여 삽입을 구현한다.
 */
interface R2dbcTypeBatchUpsertRepository {
    val databaseClient: DatabaseClient
    val log: Logger
    fun saveAll(entities: List<R2dbcTypeEntity>, createdBy: String, createdAt: Instant): Flux<R2dbcTypeEntity> {
        val constantCsv = listOf(escapeSql(createdBy), formatTimestampLiteral(createdAt)).joinToString(separator = ", ", prefix = ", ")
        val values = buildString {
            entities.joinTo(this, separator = ",") { "(${it.toCsv()}$constantCsv)" }
        }
        val sql = "$INSERT_TYPE_SQL VALUES $values"
        log.info(sql)
        return databaseClient.sql(sql).fetch().rowsUpdated()
            .delayUntil { saveAllAttributes(entities) }
            .thenMany(Flux.fromIterable(entities))
    }
    private fun saveAllAttributes(entities: List<R2dbcTypeEntity>): Mono<Long> {
        val values = buildString {
            entities.flatMap { it.attributes }.joinTo(this, separator = ",") { "(${it.toCsv()})" }
        }
        val sql = "$INSERT_TYPE_ATTRIBUTE_SQL VALUES $values"
        log.info(sql)
        return databaseClient.sql(sql).fetch().rowsUpdated()
    }
    companion object {
        private const val INSERT_TYPE_SQL = """
            INSERT INTO type (workspace, id, name, version, parent, effective_at, expire_at, 
            description, primitive, x, y, width, height, created_by, created_at) 
        """
        private const val INSERT_TYPE_ATTRIBUTE_SQL = """
            INSERT INTO attribute (workspace, type, name, attribute_type, "order", nullable,
            description, value_validators, value_type, key_validators, key_type, reference_type,
            file_extensions) 
        """
        private val timestampFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSXXX").withZone(ZoneOffset.UTC)

        // toCsv에서 문자열 삽입 시 이 함수를 사용하여 escape 하여 injection 방지
        private fun escapeSql(value: String?): String {
            if (value == null) return "NULL" // SQL의 NULL 값으로 처리
            val escapedValue = value.replace("'", "''")
            return "'$escapedValue'"
        }

        private fun formatNumberLiteral(instant: Number?): String = if (instant == null) "NULL" else "$instant"
        private fun formatTimestampLiteral(instant: Instant?): String = if (instant == null) "NULL"
        else "'${timestampFormatter.format(instant)}'"
        fun R2dbcTypeEntity.toCsv(): String = """
            ${escapeSql(workspace.toString())},
            ${escapeSql(id.toString())},
            ${escapeSql(name)},
            ${escapeSql(version)},
            ${escapeSql(parent)},
            ${formatTimestampLiteral(effectDateTime)},
            ${formatTimestampLiteral(expireDateTime)},
            ${escapeSql(description)},
            $primitive,
            ${formatNumberLiteral(x)},
            ${formatNumberLiteral(y)},
            ${formatNumberLiteral(width)},
            ${formatNumberLiteral(height)}
        """
        fun R2dbcAttributeEntity.toCsv(): String = """
            ${escapeSql(workspace.toString())},
            ${escapeSql(type.toString())},
            ${escapeSql(name)},
            ${escapeSql(attributeType.name)},
            $order,
            $nullable,
            ${escapeSql(description)},
            ${/*escapeSql(valueValidators)*/ "NULL"},
            ${escapeSql(valueType?.name)},
            ${/*escapeSql(keyValidators)*/ "NULL"},
            ${escapeSql(keyType?.name)},
            ${escapeSql(referenceType)},
            ${escapeSql(fileExtensions)}
        """.trimIndent()
    }
}