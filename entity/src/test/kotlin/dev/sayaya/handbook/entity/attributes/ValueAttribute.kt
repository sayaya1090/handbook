package dev.sayaya.handbook.entity.attributes

import dev.sayaya.handbook.entity.Attribute
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import java.io.Serializable

@Entity
@DiscriminatorValue("Value")
internal class ValueAttribute: Attribute() {
    @Column(name="value_validators", columnDefinition = "jsonb") lateinit var validators: Serializable
}