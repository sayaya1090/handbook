package dev.sayaya.handbook.entity.attributes

import dev.sayaya.handbook.entity.Attribute
import dev.sayaya.handbook.entity.AttributeType
import dev.sayaya.handbook.entity.Type
import jakarta.persistence.*
import java.io.Serializable

@Entity
@DiscriminatorValue("Array")
internal class ArrayAttribute: Attribute() {
    @Enumerated(EnumType.STRING) @Column(name="value_type") lateinit var valueType: AttributeType
    // @Column(name="value_validators", columnDefinition = "jsonb") var validators: String? = null
    companion object {
        fun of(type: Type, name: String, valueType: AttributeType, validators: String? = null) = ArrayAttribute().apply {
            this.type = type
            this.name = name
            this.valueType = valueType
            // this.validators = validators
        }
    }
}
