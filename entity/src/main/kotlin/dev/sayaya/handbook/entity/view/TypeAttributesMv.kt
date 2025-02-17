package dev.sayaya.handbook.entity.view

import jakarta.persistence.*
import java.io.Serializable
import java.time.Instant

/*@Table(name = "type_attributes", indexes=[
    Index(columnList = "type, name"),
]) @Entity --MV */
@IdClass(TypeAttributesMv.Companion.TypeAttributesMvId::class)
data class TypeAttributesMv(
    @Id @Column(nullable = false)
    val type: String,
    val attributeType: String? = null,
    @Id @Column(nullable = false)
    val name: String,
    val description: String? = null,
    @Column(nullable = false)
    val nullable: Boolean,
    val valueType: String? = null,
    val referenceType: String? = null,
    @Column(name = "effective_at", nullable = false) val effectiveDateTime: Instant,
    @Column(name = "expire_at", nullable = false) val expiryDateTime: Instant
) {
    companion object {
        @JvmRecord
        data class TypeAttributesMvId (
            val type: String = "",
            val name: String = ""
        ) : Serializable
    }
}
