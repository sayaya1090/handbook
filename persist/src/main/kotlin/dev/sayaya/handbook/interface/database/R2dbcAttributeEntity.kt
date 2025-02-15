package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.AttributeType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.io.Serializable

// 공통 속성을 가진 상위 엔티티
@Table("attribute")
data class R2dbcAttributeEntity (
    @Id val id: R2dbcAttributeId,
    val attributeType: AttributeType,
    val keyType: AttributeType?,
    val valueType: AttributeType?,
    val referenceType: String?,
    val fileExtensions: String?,
    var description: String,
    var nullable: Boolean,
) {
    companion object {
        data class R2dbcAttributeId (
            val type: String,
            val name: String
        ) : Serializable
    }
}