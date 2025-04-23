package dev.sayaya.handbook.`interface`.database

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("workspace")
data class R2dbcWorkspaceEntity (
    val id: UUID
) {
    var name: String = ""
    var description: String = ""
    @CreatedDate @Column("created_at") lateinit var createDateTime: Instant
    @CreatedBy @Column("created_by") lateinit var createBy: UUID
    @LastModifiedDate @Column("last_modified_at") lateinit var lastModifyDateTime: Instant
    @LastModifiedBy @Column("last_modified_by") lateinit var lastModifyBy: UUID

    companion object {
        fun of(id: UUID = UUID.randomUUID(), name: String, description: String = "") = R2dbcWorkspaceEntity(
            id = id
        ).apply {
            this.name = name
            this.description = description
        }
    }
}