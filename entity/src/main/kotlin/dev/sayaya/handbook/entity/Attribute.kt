package dev.sayaya.handbook.entity

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.io.Serializable

@Table(name = "attribute")
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "attribute_type", discriminatorType = DiscriminatorType.STRING)
internal abstract class Attribute {
    @EmbeddedId private val pk: AttributeId = AttributeId()
    @Column(nullable = false) open var primitive: Boolean = false
    @Column(nullable = false) open var nullable: Boolean = false
    @Column open var description: String? = null

    companion object {
        @Embeddable
        @JvmRecord
        data class AttributeId(
            @ManyToOne @OnDelete(action = OnDeleteAction.CASCADE) @JoinColumn(name = "type", nullable = false) val type: Type = Type(),
            @Column(name = "name", length = 32, nullable = false, updatable = false) val name: String = ""
        ) : Serializable
    }
}