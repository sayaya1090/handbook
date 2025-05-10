package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.AttributeTypeDefinition
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Table
import java.io.Serializable
import java.util.*

@Table("attribute")
data class R2dbcAttributeEntity (
    val workspace: UUID,
    val type: UUID,
    val name: String,
    val order: Short,
    val attributeType: AttributeTypeDefinition,
    var description: String?,
    var nullable: Boolean,
) {
    @Transient @Id val id: R2dbcAttributeId = R2dbcAttributeId(workspace, type, name)

    companion object {
        data class R2dbcAttributeId (val workspace: UUID, val type: UUID, val name: String) : Serializable
    }
}