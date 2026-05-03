package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.Type
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

/**
 * `types` 테이블의 **읽기 전용 투영** — type-query 전용.
 */
@Table("types")
data class R2dbcTypeEntity(
    @Id val id: String,
    val version: String,
    val workspace: UUID,
    @Column("effect_date_time") val effectDateTime: Instant,
    @Column("expire_date_time") val expireDateTime: Instant,
    val description: String?,
    val primitive: Boolean,
    val parent: String?,
    val rev: Long? = null,
) {
    fun toDomain(attributes: List<Attribute> = emptyList()): Type {
        val type = Type.create(id, version, effectDateTime.toEpochMilli().toDouble(), expireDateTime.toEpochMilli().toDouble())
        type.description(description)
        type.primitive(primitive)
        type.parent(parent)
        type.attributes(attributes.toTypedArray())
        return type
    }
}
