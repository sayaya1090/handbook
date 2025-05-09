package dev.sayaya.handbook.entity.attributes

import dev.sayaya.handbook.entity.Attribute
import dev.sayaya.handbook.entity.Type
import jakarta.persistence.*

@Entity
@DiscriminatorValue("Document")
internal class DocumentAttribute: Attribute() {
    @Column(length = 64, name="reference_type") lateinit var referenceType: String
    companion object {
        fun of(type: Type, name: String, index: Short, referenceType: String) = DocumentAttribute().apply {
            this.type(type)
            this.name(name)
            this.order = index
            this.referenceType = referenceType
        }
    }
}