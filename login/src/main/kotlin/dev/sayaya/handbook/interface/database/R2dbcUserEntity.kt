package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.State
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table("public.user")
data class R2dbcUserEntity(
    @Id private val id: UUID,
    // val roles: MutableList<Role>
    val provider: String,
    val account: String,
    val name: String
): Persistable<UUID> {
    var state: State = State.ACTIVATED
    @CreatedDate @Column("created_at") lateinit var createDateTime: LocalDateTime
    @Column("last_login_at") var lastLoginDateTime: LocalDateTime = LocalDateTime.now()
    @LastModifiedDate @Column("last_modified_at") lateinit var lastModifyDateTime: LocalDateTime
    override fun getId(): UUID = id
    override fun isNew(): Boolean = this::createDateTime.isInitialized.not()
}