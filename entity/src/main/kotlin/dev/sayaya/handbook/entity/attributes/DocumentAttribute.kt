package dev.sayaya.handbook.entity.attributes

import dev.sayaya.handbook.entity.Attribute
import dev.sayaya.handbook.entity.Type
import jakarta.persistence.*

@Entity
@DiscriminatorValue("Document")
internal class DocumentAttribute: Attribute() {
    @ManyToOne @JoinColumn(name="reference_type") lateinit var referenceType: Type
    companion object {
        fun of(type: Type, name: String, referenceType: Type) = DocumentAttribute().apply {
            this.type = type
            this.name = name
            this.referenceType = referenceType
        }
    }
}