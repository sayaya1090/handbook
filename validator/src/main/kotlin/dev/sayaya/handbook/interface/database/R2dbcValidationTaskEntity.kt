package dev.sayaya.handbook.`interface`.database

import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("validation_task")
data class R2dbcValidationTaskEntity (
    val workspace: UUID,
    val id: UUID,
    val document: UUID,
) {

}