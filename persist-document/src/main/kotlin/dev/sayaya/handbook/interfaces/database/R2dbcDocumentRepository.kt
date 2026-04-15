package dev.sayaya.handbook.interfaces.database

import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
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

/** Spring Data R2DBC 자동 구현 인터페이스. documents 테이블에 대한 기본 CRUD를 제공한다. */
interface R2dbcDocumentEntityRepository : ReactiveCrudRepository<R2dbcDocumentEntity, UUID>

/**
 * [DocumentRepository] 포트의 R2DBC 어댑터.
 *
 * **책임:** 문서 도메인 객체와 R2DBC 엔티티 간 변환, 트랜잭션 관리, JSONB 패치 쿼리 실행.
 *
 * **의존관계:**
 * - [R2dbcDocumentEntityRepository] — Spring Data 기본 CRUD
 * - [DatabaseClient] — 커스텀 SQL 실행 (JSONB 머지 패치)
 * - [ObjectMapper] — data 필드 JSON 직렬화/역직렬화
 * - [TransactionalOperator] — 리액티브 트랜잭션 경계
 *
 * **주의:** patchOne()은 `data = data || :patchData::jsonb` SQL로 기존 필드를 보존하면서 변경 필드만 덮어쓴다.
 * rev 불일치 시 [DuplicateKeyException]을 던져 409 Conflict로 변환된다.
 */
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

    override fun findAll(workspace: UUID, type: String?): Flux<Document> {
        val sql = if (type != null)
            "SELECT * FROM documents WHERE workspace = :workspace AND type = :type"
        else
            "SELECT * FROM documents WHERE workspace = :workspace"
        var spec = databaseClient.sql(sql).bind("workspace", workspace)
        if (type != null) spec = spec.bind("type", type)
        return spec.map { row, _ ->
            val dataJson = row.get("data", String::class.java) ?: "{}"
            val dataMap: Map<String, String?> = objectMapper.readValue(dataJson)
            Document(
                id = row.get("id", UUID::class.java),
                type = row.get("type", String::class.java)!!,
                serial = row.get("serial", String::class.java)!!,
                effectDateTime = row.get("effect_date_time", java.time.Instant::class.java)!!,
                expireDateTime = row.get("expire_date_time", java.time.Instant::class.java)!!,
                createDateTime = row.get("create_date_time", java.time.Instant::class.java),
                creator = row.get("creator", String::class.java),
                data = dataMap,
                status = row.get("status", String::class.java) ?: "DRAFT",
                rev = row.get("rev", java.lang.Long::class.java)?.toLong(),
            )
        }.all()
    }

    override fun findById(id: UUID): Mono<Document> {
        return repo.findById(id).map { it.toDomainWithData(objectMapper) }
    }

    override fun updateStatus(id: UUID, status: String): Mono<Document> {
        return databaseClient.sql("""
            UPDATE documents SET status = :status WHERE id = :id
        """.trimIndent())
            .bind("status", status)
            .bind("id", id)
            .fetch().rowsUpdated()
            .flatMap { rowsUpdated ->
                if (rowsUpdated == 0L) Mono.error(IllegalArgumentException("Document not found: $id"))
                else repo.findById(id).map { it.toDomainWithData(objectMapper) }
            }
    }

    override fun deleteAll(workspace: UUID, documents: List<Document>): Mono<Void> {
        val ids = documents.mapNotNull { it.id }
        return repo.deleteAllById(ids)
            .`as`(tx::transactional)
    }

    private fun R2dbcDocumentEntity.toDomainWithData(objectMapper: ObjectMapper): Document {
        val dataMap: Map<String, String?> = objectMapper.readValue(data.asString())
        return Document(
            id = id,
            type = type,
            serial = serial,
            effectDateTime = effectDateTime,
            expireDateTime = expireDateTime,
            createDateTime = createDateTime,
            creator = creator,
            data = dataMap,
            status = status,
            rev = rev,
        )
    }
}
