package dev.sayaya.handbook.interfaces.database

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("documents")
class R2dbcDocumentEntity(
    val workspace: UUID,
    val id: UUID,
    val type: String,
    val serial: String,
    @Column("effect_date_time") val effectDateTime: Instant,
    @Column("expire_date_time") val expireDateTime: Instant,
    @Column("create_date_time") val createDateTime: Instant,
    val creator: String,
    val data: String,
    val rev: Long? = null,
    override val count: Long = -1,
) : EntityPageable

interface EntityPageable {
    @get:Column("count") val count: Long
}
