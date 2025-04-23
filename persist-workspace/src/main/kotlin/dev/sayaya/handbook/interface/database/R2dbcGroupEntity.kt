package dev.sayaya.handbook.`interface`.database

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("\"group\"")
data class R2dbcGroupEntity (
    val workspace: UUID,
    val name: String
) {
    @CreatedDate @Column("created_at") lateinit var createDateTime: Instant
    @CreatedBy @Column("created_by") lateinit var createBy: UUID
    @LastModifiedDate @Column("last_modified_at") lateinit var lastModifyDateTime: Instant
    @LastModifiedBy @Column("last_modified_by") lateinit var lastModifyBy: UUID

    companion object {
        fun of(workspace: UUID, name: String, description: String = "") = R2dbcGroupEntity(
            workspace = workspace,
            name = name,
        )
    }
}