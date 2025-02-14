package dev.sayaya.handbook.entity.attributes

import dev.sayaya.handbook.entity.Attribute
import dev.sayaya.handbook.entity.Type
import jakarta.persistence.*

@Entity
@DiscriminatorValue("Document")
internal class DocumentAttribute: Attribute() {
    @ManyToOne @JoinColumn(name="reference_type") lateinit var referenceType: Type
}