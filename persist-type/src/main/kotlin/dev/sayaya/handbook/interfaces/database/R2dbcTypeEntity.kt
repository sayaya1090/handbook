package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.Type
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

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
    @Version val rev: Long? = null,
) {
    fun toDomain(attributes: List<Attribute> = emptyList()): Type = Type(
        id = id,
        version = version,
        effectDateTime = effectDateTime,
        expireDateTime = expireDateTime,
        description = description,
        primitive = primitive,
        parent = parent,
    )

    companion object {
        fun fromDomain(workspace: UUID, type: Type): R2dbcTypeEntity = R2dbcTypeEntity(
            id = type.id,
            version = type.version,
            workspace = workspace,
            effectDateTime = type.effectDateTime,
            expireDateTime = type.expireDateTime,
            description = type.description,
            primitive = type.primitive,
            parent = type.parent,
        )
    }
}
