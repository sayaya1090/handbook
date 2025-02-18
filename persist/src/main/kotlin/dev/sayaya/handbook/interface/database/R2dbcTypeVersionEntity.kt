package dev.sayaya.handbook.`interface`.database

import org.springframework.data.annotation.*
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.io.Serializable
import java.time.Instant

@Table("type_version")
data class R2dbcTypeVersionEntity (
    var type: String,
    var version: String,
    @Column("effective_at") val effectiveDateTime: Instant,
    @Column("expire_at") val expiryDateTime: Instant
) {
    @Transient @Id val id: R2dbcTypeVersionId = R2dbcTypeVersionId(type, version)

    @CreatedDate @Column("created_at") lateinit var createDateTime: Instant
    @CreatedBy @Column("created_by") lateinit var createBy: String
    companion object {
        data class R2dbcTypeVersionId (
            val type: String,
            val version: String
        ) : Serializable

        fun of(type: String, version: String, effectiveDateTime: Instant, expiryDateTime: Instant) = R2dbcTypeVersionEntity(
            type = type,
            version = version,
            effectiveDateTime = effectiveDateTime,
            expiryDateTime = expiryDateTime
        )
    }
}