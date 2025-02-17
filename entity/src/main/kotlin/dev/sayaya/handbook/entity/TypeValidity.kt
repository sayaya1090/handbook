package dev.sayaya.handbook.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import java.io.Serializable
import java.time.Instant
import java.util.*

@Table(name = "type_validity")
@Entity
@IdClass(TypeValidity.Companion.TypeValidityId::class)
internal class TypeValidity {
    @Id @ManyToOne @JoinColumn(name = "type") lateinit var type: Type
    @Id lateinit var id: UUID
    @CreatedDate @Column(name = "created_at", nullable = false) lateinit var createDateTime: Instant
    @CreatedBy @ManyToOne @JoinColumn(name = "created_by", nullable = false) lateinit var createBy: User
    @Column(name = "effective_at", nullable = false) lateinit var effectiveDateTime: Instant
    @Column(name = "expire_at", nullable = false) lateinit var expiryDateTime: Instant

    companion object {
        class TypeValidityId : Serializable {
            lateinit var type: Type
            lateinit var id: UUID
        }

        fun of(id: UUID = UUID.randomUUID(), user: User, type: Type, effectiveDateTime: Instant, expiryDateTime: Instant) = TypeValidity().apply {
            this.id = id
            this.type = type
            createDateTime = Instant.now()
            createBy = user
            this.effectiveDateTime = effectiveDateTime
            this.expiryDateTime = expiryDateTime
        }
    }
}