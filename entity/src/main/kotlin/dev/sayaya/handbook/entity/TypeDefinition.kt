package dev.sayaya.handbook.entity

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import java.time.Instant
import java.util.*

@Table(name = "type_definition")
@Entity
internal class TypeDefinition {
    @Id lateinit var id: UUID
    @ManyToOne @JoinColumns(value = [
        JoinColumn(name = "type", referencedColumnName="type", nullable = false),
        JoinColumn(name = "version", referencedColumnName="version", nullable = false)
    ]) @OnDelete(action = OnDeleteAction.CASCADE) lateinit var typeVersion: TypeVersion
    @CreatedDate @Column(name = "created_at", nullable = false) lateinit var createDateTime: Instant
    @CreatedBy @ManyToOne @JoinColumn(name = "created_by", nullable = false) lateinit var createBy: User
    @Column(columnDefinition = "text", nullable = false) var description: String = ""
    @Column(nullable = false) var primitive: Boolean = false
    @Column(name="\"last\"", nullable = false, columnDefinition = "boolean DEFAULT true") var last: Boolean = false
    companion object {
        fun of(id: UUID = UUID.randomUUID(), user: User, typeVersion: TypeVersion) = TypeDefinition().apply {
            this.id = id
            this.typeVersion = typeVersion
            description = typeVersion.type.id
            createDateTime = Instant.now()
            createBy = user
        }
    }
}