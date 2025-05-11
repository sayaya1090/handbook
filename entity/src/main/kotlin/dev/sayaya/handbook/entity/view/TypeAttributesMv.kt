package dev.sayaya.handbook.entity.view

import jakarta.persistence.*
import java.io.Serializable
import java.time.Instant
import java.util.*

/*@Table(name = "type_attributes", indexes=[
    Index(columnList = "type, name"),
    Index(columnList = "effective_at, expire_at, type")
]) @Entity --MV */
@IdClass(TypeAttributesMv.Companion.TypeAttributesMvId::class)
data class TypeAttributesMv(
    @Id @Column(nullable = false)
    val workspace: UUID,
    @Id @Column(nullable = false)
    val type: String,
    val version: String,
    val attributeType: String? = null,
    @Id @Column(nullable = false)
    val name: String,
    val order: Short,
    val description: String? = null,
    @Column(nullable = false)
    val nullable: Boolean,
    @Column(name = "effective_at", nullable = false) val effectiveDateTime: Instant,
    @Column(name = "expire_at", nullable = false) val expiryDateTime: Instant
) {
    companion object {
        @JvmRecord
        data class TypeAttributesMvId (
            val workspace: UUID = UUID.randomUUID(),
            val type: String = "",
            val name: String = ""
        ) : Serializable
    }
}
