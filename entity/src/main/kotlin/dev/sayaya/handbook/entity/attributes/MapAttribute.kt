package dev.sayaya.handbook.entity.attributes

import dev.sayaya.handbook.entity.Attribute
import dev.sayaya.handbook.entity.AttributeType
import dev.sayaya.handbook.entity.Type
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.io.Serializable

@Entity
@DiscriminatorValue("Map")
internal class MapAttribute: Attribute(), Attribute.Companion.HasKeyType, Attribute.Companion.HasValueType {
    @Enumerated(EnumType.STRING) @Column(name="key_type") override lateinit var keyType: AttributeType
    @Enumerated(EnumType.STRING) @Column(name="value_type") override lateinit var valueType: AttributeType
    @JdbcTypeCode(SqlTypes.JSON) @Column(name="key_validators", columnDefinition = "jsonb") var keyValidators: Serializable? = null
    @JdbcTypeCode(SqlTypes.JSON) @Column(name="value_validators", columnDefinition = "jsonb") var valueValidators: Serializable? = null
    companion object {
        fun of(type: Type, name: String, index: Short,
               keyType: AttributeType, valueType: AttributeType,
               keyValidators: Serializable? = null, valueValidators: Serializable? = null) = MapAttribute().apply {
            this.type(type)
            this.name(name)
            this.order = index
            this.keyType = keyType
            this.valueType = valueType
            this.keyValidators = keyValidators
            this.valueValidators = valueValidators
        }
    }
}