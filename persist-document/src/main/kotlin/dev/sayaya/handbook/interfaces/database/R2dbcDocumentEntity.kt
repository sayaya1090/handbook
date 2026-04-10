package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Document
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("documents")
data class R2dbcDocumentEntity(
    @Id val id: UUID,
    val workspace: UUID,
    val type: String,
    val serial: String,
    @Column("effect_date_time") val effectDateTime: Instant,
    @Column("expire_date_time") val expireDateTime: Instant,
    val data: String,
    @CreatedDate @Column("create_date_time") var createDateTime: Instant? = null,
    @CreatedBy var creator: String? = null,
    @Version val rev: Long? = null,
) {
    fun toDomain(): Document = Document(
        id = id,
        type = type,
        serial = serial,
        effectDateTime = effectDateTime,
        expireDateTime = expireDateTime,
        createDateTime = createDateTime,
        creator = creator,
        data = emptyMap(), // JSON 역직렬화는 Adapter에서 처리
    )

    companion object {
        fun fromDomain(workspace: UUID, document: Document, serializedData: String): R2dbcDocumentEntity = R2dbcDocumentEntity(
            id = document.id ?: UUID.randomUUID(),
            workspace = workspace,
            type = document.type,
            serial = document.serial,
            effectDateTime = document.effectDateTime,
            expireDateTime = document.expireDateTime,
            data = serializedData,
            createDateTime = document.createDateTime,
            creator = document.creator,
        )
    }
}
