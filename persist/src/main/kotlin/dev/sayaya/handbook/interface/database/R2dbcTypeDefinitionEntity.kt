package dev.sayaya.handbook.`interface`.database

import com.github.f4b6a3.ulid.Ulid
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("type_definition")
data class R2dbcTypeDefinitionEntity (
    @Id private val id: UUID,
    val type: String,
    val version: String
): Persistable<UUID> {
    var description: String = ""
    var primitive: Boolean = false
    @CreatedDate @Column("created_at") lateinit var createDateTime: Instant
    @CreatedBy @Column("created_by") lateinit var createdBy: String
    override fun getId(): UUID = id
    override fun isNew(): Boolean = this::createDateTime.isInitialized.not()
    companion object {
        fun of(id: UUID = Ulid.fast().toUuid(), typeVersion: R2dbcTypeVersionEntity, description: String = "", primitive: Boolean = false) = R2dbcTypeDefinitionEntity(
            id = id,
            type = typeVersion.type,
            version = typeVersion.version
        ).apply {
            this.description = description
            this.primitive = primitive
        }
    }
}