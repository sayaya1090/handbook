package dev.sayaya.handbook.interfaces.database

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("workspace")
data class R2dbcWorkspaceEntity(
    val id: UUID,
    var name: String,
    var description: String?,
    @Version var version: Long? = null,
) {
    @CreatedDate @Column("created_at") lateinit var createdAt: Instant
    @CreatedBy @Column("created_by") lateinit var createdBy: UUID
    @LastModifiedDate @Column("last_modified_at") lateinit var lastModifiedAt: Instant
    @LastModifiedBy @Column("last_modified_by") lateinit var lastModifiedBy: UUID
}
