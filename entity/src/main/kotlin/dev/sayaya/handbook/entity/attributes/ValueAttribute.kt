package dev.sayaya.handbook.entity.attributes

import dev.sayaya.handbook.entity.Attribute
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.io.Serializable

@Entity
@DiscriminatorValue("Value")
internal class ValueAttribute: Attribute() {
    @JdbcTypeCode(SqlTypes.JSON) @Column(name="value_validators", columnDefinition = "jsonb") var validators: Serializable? = null
    companion object {
        fun of(type: dev.sayaya.handbook.entity.Type, name: String, validators: Serializable? = null) = ValueAttribute().apply {
            this.type = type
            this.name = name
            this.validators = validators
        }
    }
}