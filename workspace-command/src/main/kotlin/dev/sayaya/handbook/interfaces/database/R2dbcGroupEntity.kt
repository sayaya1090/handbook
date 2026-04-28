package dev.sayaya.handbook.interfaces.database

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("group")
data class R2dbcGroupEntity(
    @Id private val id: UUID,
    val workspace: UUID,
    val name: String,
    val description: String? = null,
) : Persistable<UUID> {
    @CreatedDate @Column("created_at") lateinit var createdAt: Instant
    @CreatedBy @Column("created_by") lateinit var createdBy: UUID

    @Transient
    private var isNew: Boolean = true

    override fun getId(): UUID = id
    override fun isNew(): Boolean = isNew

    fun markNotNew(): R2dbcGroupEntity {
        this.isNew = false
        return this
    }
}
