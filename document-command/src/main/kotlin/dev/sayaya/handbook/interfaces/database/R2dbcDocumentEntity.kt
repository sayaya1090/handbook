package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Document
import io.r2dbc.postgresql.codec.Json
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

/**
 * documents 테이블에 매핑되는 R2DBC 엔티티.
 *
 * **책임:** 문서 도메인 객체와 DB 행 간의 양방향 변환을 담당한다.
 * data 컬럼은 JSONB 문자열로 저장되며, JSON 역직렬화는 [R2dbcDocumentRepositoryAdapter][dev.sayaya.handbook.interfaces.database.R2dbcDocumentRepositoryAdapter]에서 처리한다.
 *
 * **주의:** [toDomain]은 data를 빈 맵으로 반환한다. 실제 데이터가 필요하면
 * Adapter의 toDomainWithData()를 사용해야 한다.
 */
@Table("documents")
data class R2dbcDocumentEntity(
    @Id val id: UUID,
    val workspace: UUID,
    val type: String,
    val serial: String,
    @Column("effect_date_time") val effectDateTime: Instant,
    @Column("expire_date_time") val expireDateTime: Instant,
    val data: Json,
    val status: String = "DRAFT",
    @CreatedDate @Column("create_date_time") var createDateTime: Instant? = null,
    @CreatedBy var creator: String? = null,
    @Version val rev: Long? = null,
) {
    fun toDomain(): Document {
        val doc = Document.create(
            id.toString(),
            type,
            serial,
            effectDateTime.toEpochMilli().toDouble(),
            expireDateTime.toEpochMilli().toDouble(),
            createDateTime?.toEpochMilli()?.toDouble() ?: 0.0,
            creator,
            null
        )
        doc.status(status)
        rev?.let { doc.rev(it) }
        return doc
    }

    companion object {
        fun fromDomain(workspace: UUID, document: Document, serializedData: String): R2dbcDocumentEntity = R2dbcDocumentEntity(
            id = document.id()?.let { UUID.fromString(it) } ?: UUID.randomUUID(),
            workspace = workspace,
            type = document.type(),
            serial = document.serial(),
            effectDateTime = Instant.ofEpochMilli(document.effectDateTime().toLong()),
            expireDateTime = Instant.ofEpochMilli(document.expireDateTime().toLong()),
            data = Json.of(serializedData),
            status = document.status() ?: "DRAFT",
            createDateTime = if (document.createDateTime() > 0) Instant.ofEpochMilli(document.createDateTime().toLong()) else null,
            creator = document.creator(),
            rev = if (document.rev() == -1L) null else document.rev(),
        )
    }
}
