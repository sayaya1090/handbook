package dev.sayaya.handbook.interfaces.database

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("group_member")
data class R2dbcGroupMemberEntity(
    val workspace: UUID,
    @Column("group") val group: String,
    val member: UUID,
)
