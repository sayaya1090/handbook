package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.AttributeType
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Table
import java.io.Serializable
import java.util.*

@Table("attribute")
data class R2dbcAttributeEntity (
    val type: UUID,
    val name: String,
    val attributeType: AttributeType,
    val keyType: AttributeType?,
    val valueType: AttributeType?,
    val referenceType: String?,
    val fileExtensions: String?,
    var description: String?,
    var nullable: Boolean,
) {
    @Transient @Id val id: R2dbcAttributeId = R2dbcAttributeId(type, name)

    companion object {
        data class R2dbcAttributeId (val type: UUID, val name: String) : Serializable
    }
}