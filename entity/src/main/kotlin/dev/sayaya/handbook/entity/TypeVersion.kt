package dev.sayaya.handbook.entity

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import java.io.Serializable
import java.time.Instant

@Table(name = "type_version")
@Entity
@IdClass(TypeVersion.Companion.TypeVersionId::class)
internal class TypeVersion {
    @Id @ManyToOne @JoinColumn(name = "type") @OnDelete(action = OnDeleteAction.CASCADE) lateinit var type: Type
    @Id lateinit var version: String
    @CreatedDate @Column(name = "created_at", nullable = false) lateinit var createDateTime: Instant
    @CreatedBy @ManyToOne @JoinColumn(name = "created_by", nullable = false) lateinit var createBy: User
    @Column(name = "effective_at", nullable = false) lateinit var effectiveDateTime: Instant
    @Column(name = "expire_at", nullable = false) lateinit var expiryDateTime: Instant

    companion object {
        class TypeVersionId : Serializable {
            lateinit var type: Type
            lateinit var version: String
        }

        fun of(user: User, type: Type, version: String, effectiveDateTime: Instant, expiryDateTime: Instant) = TypeVersion().apply {
            this.type = type
            this.version = version
            createDateTime = Instant.now()
            createBy = user
            this.effectiveDateTime = effectiveDateTime
            this.expiryDateTime = expiryDateTime
        }
    }
}