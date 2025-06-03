package dev.sayaya.handbook.usecase.validator

import dev.sayaya.handbook.domain.AttributeTypeDefinition
import org.springframework.stereotype.Component

@Component
class ArrayValidator(
    private val validators: List<AttributeValidator<*>>
): AttributeValidator<AttributeTypeDefinition.Companion.ArrayType> {
    override val supportedAttributeType = AttributeTypeDefinition.Companion.ArrayType::class
    override fun validate(value: Any?, definition: AttributeTypeDefinition.Companion.ArrayType): Boolean {
        val child = definition.type
        return validators.none { it.validate(child, value).not() }
    }
}