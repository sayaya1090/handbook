package dev.sayaya.handbook.interfaces.database

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("\"group\"")
data class R2dbcGroupEntity(
    val workspace: UUID,
    val name: String,
) {
    @CreatedDate @Column("created_at") lateinit var createdAt: Instant
    @CreatedBy @Column("created_by") lateinit var createdBy: UUID
}
