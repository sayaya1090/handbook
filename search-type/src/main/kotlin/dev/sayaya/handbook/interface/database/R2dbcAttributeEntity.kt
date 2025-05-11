package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.AttributeTypeDefinition
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("attribute")
data class R2dbcAttributeEntity (
    val workspace: UUID,
    val type: UUID,
    val order: Short,
    val name: String,
    val attributeType: AttributeTypeDefinition,
    val description: String?,
    val nullable: Boolean,
)