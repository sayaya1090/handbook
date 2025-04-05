package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.AttributeType
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("attribute")
data class R2dbcAttributeEntity (
    val workspace: UUID,
    val type: UUID,
    val name: String,
    val attributeType: AttributeType,
    val keyType: AttributeType?,
    val valueType: AttributeType?,
    val referenceType: String?,
    val fileExtensions: String?,
    val description: String?,
    val nullable: Boolean,
)