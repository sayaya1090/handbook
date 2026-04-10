package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.State
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table("users")
data class R2dbcUserEntity(
    @Id private val id: UUID,
    val provider: String,
    val account: String,
    var name: String,
    var state: State = State.ACTIVATED,
    @CreatedDate @Column("created_at") var createDateTime: LocalDateTime? = null,
    @Column("last_login_at") var lastLoginDateTime: LocalDateTime? = null,
    @LastModifiedDate @Column("last_modified_at") var lastModifyDateTime: LocalDateTime? = null,
) : Persistable<UUID> {
    @org.springframework.data.annotation.Transient
    @JvmField
    var new: Boolean = false

    override fun getId(): UUID = id
    override fun isNew(): Boolean = new
}
