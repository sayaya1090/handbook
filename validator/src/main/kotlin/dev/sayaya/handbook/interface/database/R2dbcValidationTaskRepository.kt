package dev.sayaya.handbook.`interface`.database

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.f4b6a3.ulid.Ulid
import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.usecase.ValidationTaskRepository
import io.r2dbc.postgresql.codec.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
@EnableR2dbcAuditing
class R2dbcValidationTaskRepository (
    private val template: R2dbcEntityTemplate,
    private val objectMapper: ObjectMapper
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

    override fun save(workspace: UUID, document: Document, result: Map<String, Boolean>): Mono<Void> {
        val jsonResult = try {
            Json.of(objectMapper.writeValueAsString(result))
        } catch (e: Exception) {
            log.error("Failed to convert validation result to JSON for document ${document.id}", e)
            return Mono.error(e) // JSON 변환 실패 시 에러 반환
        }

        val entity = R2dbcValidationTaskEntity(
            workspace = workspace,
            id = Ulid.fast().toUuid(),
            document = document.id!!
        ).apply {
            results = jsonResult
            status = "COMPLETED"
        }
        return template.insert(entity).then()
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
