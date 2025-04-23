package dev.sayaya.handbook.`interface`.database

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Table
import java.io.Serializable
import java.util.*

@Table("group_member")
data class R2dbcGroupMemberEntity (
    val workspace: UUID,
    val group: String,
    val member: UUID
) {
    @Transient @Id val pk: R2dbcGroupMemberId = R2dbcGroupMemberId(workspace, group, member)
    companion object {
        data class R2dbcGroupMemberId (val workspace: UUID, val group: String, val member: UUID) : Serializable
    }
}