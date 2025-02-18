package dev.sayaya.handbook.entity.attributes

import dev.sayaya.handbook.entity.Attribute
import dev.sayaya.handbook.entity.AttributeType
import dev.sayaya.handbook.entity.Type
import dev.sayaya.handbook.entity.TypeDefinition
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.io.Serializable

@Entity
@DiscriminatorValue("Array")
internal class ArrayAttribute: Attribute() {
    @Enumerated(EnumType.STRING) @Column(name="value_type") lateinit var valueType: AttributeType
    @JdbcTypeCode(SqlTypes.JSON) @Column(name="value_validators", columnDefinition = "jsonb") var validators: Serializable? = null
    companion object {
        fun of(type: TypeDefinition, name: String, valueType: AttributeType, validators: Serializable? = null) = ArrayAttribute().apply {
            this.type = type
            this.name = name
            this.valueType = valueType
            this.validators = validators
        }
    }
}
