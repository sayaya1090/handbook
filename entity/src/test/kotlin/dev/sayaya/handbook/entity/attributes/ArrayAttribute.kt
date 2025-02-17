package dev.sayaya.handbook.entity.attributes

import dev.sayaya.handbook.entity.Attribute
import dev.sayaya.handbook.entity.AttributeType
import jakarta.persistence.*
import java.io.Serializable

@Entity
@DiscriminatorValue("Array")
internal class ArrayAttribute: Attribute() {
    @Column(name="value_validators", columnDefinition = "jsonb") lateinit var validators: Serializable
}