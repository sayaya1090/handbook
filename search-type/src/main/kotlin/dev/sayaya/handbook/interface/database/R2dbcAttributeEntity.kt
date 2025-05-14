package dev.sayaya.handbook.`interface`.database

import io.r2dbc.postgresql.codec.Json
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("attribute")
data class R2dbcAttributeEntity (
    val workspace: UUID,
    val type: UUID,
    val order: Short,
    val name: String,
    val attributeType: Json,
    val description: String?,
    val nullable: Boolean,
)