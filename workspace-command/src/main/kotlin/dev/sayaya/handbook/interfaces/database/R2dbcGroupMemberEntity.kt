package dev.sayaya.handbook.interfaces.database

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("group_member")
data class R2dbcGroupMemberEntity(
    @Id private val id: UUID,
    val workspace: UUID,
    @Column("group") val group: UUID,
    val member: UUID,
) : Persistable<UUID> {
    @CreatedDate @Column("created_at") lateinit var createdAt: Instant

    @Transient
    private var isNew: Boolean = true

    override fun getId(): UUID = id
    override fun isNew(): Boolean = isNew

    fun markNotNew(): R2dbcGroupMemberEntity {
        this.isNew = false
        return this
    }
}
