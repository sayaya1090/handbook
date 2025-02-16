package dev.sayaya.handbook.entity

import jakarta.persistence.*
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
        JoinColumn(name = "validity", referencedColumnName="id", nullable = false)
    ]) lateinit var type: TypeValidity
    @CreatedDate @Column(name = "created_at", nullable = false) lateinit var createDateTime: Instant
    @CreatedBy @ManyToOne @JoinColumn(name = "created_by", nullable = false) lateinit var createBy: User
    @Column(columnDefinition = "text", nullable = false) var description: String = ""
    @Column(nullable = false) var primitive: Boolean = false
    @Column(name="\"last\"", nullable = false, columnDefinition = "boolean DEFAULT true") var last: Boolean = false
    companion object {
        fun of(id: UUID = UUID.randomUUID(), user: User, typeValidity: TypeValidity) = TypeDefinition().apply {
            this.id = id
            this.type = typeValidity
            description = typeValidity.type.id
            createDateTime = Instant.now()
            createBy = user
        }
    }
}