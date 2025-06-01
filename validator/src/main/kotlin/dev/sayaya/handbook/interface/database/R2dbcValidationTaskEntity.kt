package dev.sayaya.handbook.`interface`.database

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.io.Serializable
import java.time.Instant
import java.util.UUID

@Table("validation_task")
data class R2dbcValidationTaskEntity(
    val workspace: UUID,
    val id: UUID,
    val type: String,
    val serial: String,
    @Column("effective_at") var effectDateTime: Instant,
    @Column("expire_at") var expireDateTime: Instant,
    val data: String
) {
    @CreatedDate @Column("created_at") lateinit var createDateTime: Instant
    @CreatedBy @Column("created_by") lateinit var createBy: UUID
    @Transient @Id val pk: R2dbcDocumentId = R2dbcDocumentId(workspace, id)
    companion object {
        data class R2dbcDocumentId (val workspace: UUID, val id: UUID) : Serializable
        fun of(workspace: UUID, id: UUID, type: String, serial: String, effectDateTime: Instant, expireDateTime: Instant, data: String): R2dbcValidationTaskEntity = R2dbcValidationTaskEntity(
            workspace = workspace,
            id = id,
            type = type,
            serial = serial,
            effectDateTime = effectDateTime,
            expireDateTime = expireDateTime,
            data = data
        )
    }
}