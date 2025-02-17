package dev.sayaya.handbook.entity.attributes

import dev.sayaya.handbook.entity.Attribute
import dev.sayaya.handbook.entity.Type
import dev.sayaya.handbook.entity.TypeDefinition
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
@DiscriminatorValue("Document")
internal class DocumentAttribute: Attribute() {
    @ManyToOne @JoinColumn(name="reference_type") @OnDelete(action = OnDeleteAction.CASCADE) lateinit var referenceType: Type
    companion object {
        fun of(type: TypeDefinition, name: String, referenceType: Type) = DocumentAttribute().apply {
            this.type = type
            this.name = name
            this.referenceType = referenceType
        }
    }
}