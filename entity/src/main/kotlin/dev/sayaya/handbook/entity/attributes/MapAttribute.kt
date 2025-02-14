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
    @Column(name="key_validators", columnDefinition = "jsonb") lateinit var keyValidators: Serializable
    @Column(name="value_validators", columnDefinition = "jsonb") lateinit var valueValidators: Serializable
}