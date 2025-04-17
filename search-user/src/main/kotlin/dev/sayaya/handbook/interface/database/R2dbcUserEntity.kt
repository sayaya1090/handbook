package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.State
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table("public.user")
data class R2dbcUserEntity (
    @Id val id: UUID,
    val provider: String,
    val account: String,
    val name: String,
    @Column("created_at") val createDateTime: LocalDateTime,
    @Column("last_login_at") val lastLoginDateTime: LocalDateTime,
    @Column("last_modified_at") val lastModifyDateTime: LocalDateTime,
    val state: State
)