package dev.sayaya.handbook.interfaces.database

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("group")
data class R2dbcGroupEntity(
    @Id val id: UUID,
    val workspace: UUID,
    val name: String,
    val description: String?,
)
