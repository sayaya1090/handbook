package dev.sayaya.handbook.`interface`.database

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("type")
data class R2dbcTypeEntity (
    val workspace: UUID,
    val id: UUID,
    val name: String,
    val version: String,
    var parent: String?,
    @Column("effective_at") val effectDateTime: Instant,
    @Column("expire_at") val expireDateTime: Instant,
    val description: String,
    val primitive: Boolean,
    @Column("created_at") val createDateTime: Instant,
    @Column("created_by") val createBy: UUID,
    val x: Short,
    val y: Short,
    val width: Short,
    val height: Short
) {
    var prev: String? = null
    var next: String? = null
}