package dev.sayaya.handbook.`interface`.database

import io.r2dbc.postgresql.codec.Json
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("document_with_validation")
data class R2dbcDocumentEntity(
    val workspace: UUID,
    val id: UUID,
    val type: String,
    val serial: String,
    @Column("effective_at") val effectDateTime: Instant,
    @Column("expire_at") val expireDateTime: Instant,
    @Column("created_at") val createDateTime: Instant,
    @Column("created_by") val creatorUserId: UUID,
    @Column("creator_name") val creatorUserName: String,
    val data: Json,
    @Column("validation_requested_at") val validationRequestDateTime: Instant?,
    @Column("validation_started_at") val validationStartDateTime: Instant?,
    @Column("validation_status") val validationStatus: String?,
    @Column("validation_results") val validationResult: Json?,
)