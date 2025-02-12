package dev.sayaya.handbook.entity

import jakarta.persistence.*
import java.io.Serializable

@Table(name = "attribute")
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "attribute_type", discriminatorType = DiscriminatorType.STRING)
internal abstract class Attribute {
    @EmbeddedId private val pk: AttributeId = AttributeId()
    @Column(nullable = false) var primitive: Boolean = false
    @Column(nullable = false) var nullable: Boolean = false
    @Column var description: String? = null

    companion object {
        @Embeddable
        class AttributeId : Serializable {
            @Column(name = "type", length = 16, nullable = false, updatable = false)
            private var type: String = ""
            @Column(name = "name", length = 32, nullable = false, updatable = false)
            private var name: String = ""
        }
    }
}