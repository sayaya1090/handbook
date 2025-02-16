package dev.sayaya.handbook.`interface`.database

import com.github.f4b6a3.ulid.Ulid
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("type")
data class R2dbcTypeEntity (
    private val id: UUID,
    val name: String,
    val version: String,
    var parent: String? = null,
    @Column("effective_at") var effectDateTime: Instant,
    @Column("expire_at") var expireDateTime: Instant
): Persistable<UUID> {
    var description: String = ""
    var primitive: Boolean = false
    @CreatedDate @Column("created_at") lateinit var createDateTime: Instant
    @CreatedBy @Column("created_by") lateinit var createBy: String
    override fun getId(): UUID = id
    override fun isNew(): Boolean = this::createDateTime.isInitialized.not()

    companion object {
        fun of(id: UUID = Ulid.fast().toUuid(), name: String, version: String, parent: String?, effectiveDateTime: Instant, expiryDateTime: Instant, description: String = "", primitive: Boolean = false) = R2dbcTypeEntity(
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