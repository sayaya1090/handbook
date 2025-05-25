package dev.sayaya.handbook.`interface`.database

import io.r2dbc.postgresql.codec.Json
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID


@Table("document_with_user")
class R2dbcDocumentEntity(
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
    override val count: Long = -1
): EntityPageable