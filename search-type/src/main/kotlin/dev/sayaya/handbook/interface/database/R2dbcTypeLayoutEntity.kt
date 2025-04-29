package dev.sayaya.handbook.`interface`.database

import org.springframework.data.relational.core.mapping.Column
import java.time.Instant
import java.util.*

data class R2dbcTypeLayoutEntity (
    val workspace: UUID,
    val id: UUID,
    val name: String,
    val version: String,
    val parent: String? = null,
    @Column("effective_at") val effectDateTime: Instant,
    @Column("expire_at") val expireDateTime: Instant,
    val description: String = "",
    val primitive: Boolean = false,
    val last: Boolean,
    @Column("created_at") val createDateTime: Instant,
    @Column("created_by") val createBy: String,

    val layout: UUID,
    val x: Short,
    val y: Short,
    val width: Short,
    val height: Short
)