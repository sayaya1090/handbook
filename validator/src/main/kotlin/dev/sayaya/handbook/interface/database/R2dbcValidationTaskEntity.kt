package dev.sayaya.handbook.`interface`.database

import io.r2dbc.postgresql.codec.Json
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("validation_task")
data class R2dbcValidationTaskEntity (
    val workspace: UUID,
    @Id val id: UUID,
    val document: UUID,
) {
    var results: Json? = null
    var status: String = "NEW"
    var priority: Int = 0
    @Column("created_at") val createDateTime: Instant = Instant.now()
    @Column("started_at") var startDateTime: Instant? = null
    @LastModifiedDate @Column("updated_at") lateinit var updateDateTime: Instant
    var retryCount: Int = 0
    var lastError: String? = null
}