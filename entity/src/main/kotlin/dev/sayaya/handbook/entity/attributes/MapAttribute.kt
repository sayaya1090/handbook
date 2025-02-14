package dev.sayaya.handbook.entity.attributes

import dev.sayaya.handbook.entity.Attribute
import dev.sayaya.handbook.entity.AttributeType
import jakarta.persistence.*
import java.io.Serializable

@Entity
@DiscriminatorValue("Map")
internal class MapAttribute: Attribute() {
    @Enumerated(EnumType.STRING) @Column(name="key_type") lateinit var keyType: AttributeType
    @Enumerated(EnumType.STRING) @Column(name="value_type") lateinit var valueType: AttributeType
    @Column(name="key_validators", columnDefinition = "jsonb") var keyValidators: String? = null
    @Column(name="value_validators", columnDefinition = "jsonb") var valueValidators: String? = null
    companion object {
        fun of(type: dev.sayaya.handbook.entity.Type, name: String,
               keyType: AttributeType, valueType: AttributeType,
               keyValidators: String? = null, valueValidators: String? = null) = MapAttribute().apply {
            this.type = type
            this.name = name
            this.keyType = keyType
            this.valueType = valueType
            this.keyValidators = keyValidators
            this.valueValidators = valueValidators
        }
    }
}