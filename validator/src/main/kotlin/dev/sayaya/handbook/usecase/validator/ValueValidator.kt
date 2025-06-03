package dev.sayaya.handbook.usecase.validator

import dev.sayaya.handbook.domain.AttributeTypeDefinition
import org.springframework.stereotype.Component

@Component
class ValueValidator: AttributeValidator<AttributeTypeDefinition.Companion.ValueType> {
    override val supportedAttributeType = AttributeTypeDefinition.Companion.ValueType::class
    override fun validate(definition: AttributeTypeDefinition.Companion.ValueType, value: Any?): Boolean {
        return definition.validators.none { def ->
            def.validate(value).not()
        }
    }
}