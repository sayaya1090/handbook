package dev.sayaya.handbook.entity.view

import jakarta.persistence.*
import java.io.Serializable

/*@Table(name = "type_attributes", indexes=[
    Index(columnList = "type, name"),
]) @Entity --MV */
@IdClass(TypeAttributesMv.Companion.TypeAttributesMvId::class)
data class TypeAttributesMv(
    @Id @Column(name = "type", nullable = false)
    val type: String,
    @Column(name = "attribute_type")
    val attributeType: String? = null,
    @Id @Column(name = "name", nullable = false)
    val name: String,
    @Column(name = "description")
    val description: String? = null,
    @Column(name = "nullable", nullable = false)
    val nullable: Boolean,
    @Column(name = "value_type")
    val valueType: String? = null,
    @Column(name = "reference_type")
    val referenceType: String? = null
) {
    companion object {
        @JvmRecord
        data class TypeAttributesMvId (
            val type: String = "",
            val name: String = ""
        ) : Serializable
    }
}
