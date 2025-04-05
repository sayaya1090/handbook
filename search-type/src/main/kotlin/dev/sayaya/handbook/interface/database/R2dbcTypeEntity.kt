package dev.sayaya.handbook.`interface`.database

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("type")
class R2dbcTypeEntity (
    val workspace: UUID,
    val id: UUID,
    val name: String,
    val version: String,
    val parent: String? = null,
    @Column("effective_at") val effectDateTime: Instant,
    @Column("expire_at") val expireDateTime: Instant,
    val description: String = "",
    val primitive: Boolean = false,
    @Column("created_at") val createDateTime: Instant,
    @Column("created_by") val createBy: String,
    override val count: Long = -1
): EntityPageable