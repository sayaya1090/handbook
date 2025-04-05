package dev.sayaya.handbook.`interface`.database

import com.github.f4b6a3.ulid.Ulid
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.io.Serializable
import java.time.Instant
import java.util.*

@Table("type")
data class R2dbcTypeEntity (
    val workspace: UUID,
    val id: UUID,
    val name: String,
    val version: String,
    var parent: String? = null,
    @Column("effective_at") var effectDateTime: Instant,
    @Column("expire_at") var expireDateTime: Instant
) {
    var description: String = ""
    var primitive: Boolean = false
    @CreatedDate @Column("created_at") lateinit var createDateTime: Instant
    @CreatedBy @Column("created_by") lateinit var createBy: String
    @Transient @Id val pk: R2dbcTypeId = R2dbcTypeId(workspace, id, name)

    companion object {
        data class R2dbcTypeId (val workspace: UUID, val type: UUID, val name: String) : Serializable
        fun of(workspace: UUID, id: UUID = Ulid.fast().toUuid(), name: String, version: String, parent: String?, effectiveDateTime: Instant, expiryDateTime: Instant, description: String = "", primitive: Boolean = false) = R2dbcTypeEntity(
            workspace = workspace,
            id = id,
            name = name,
            version = version,
            parent = parent,
            effectDateTime = effectiveDateTime,
            expireDateTime = expiryDateTime
        ).apply {
            this.description = description
            this.primitive = primitive
        }
    }
}