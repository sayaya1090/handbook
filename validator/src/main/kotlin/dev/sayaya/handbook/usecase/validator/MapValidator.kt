package dev.sayaya.handbook.usecase.validator

import dev.sayaya.handbook.domain.AttributeTypeDefinition
import org.springframework.stereotype.Component

@Component
class MapValidator(
    private val validators: List<AttributeValidator<*>>
): AttributeValidator<AttributeTypeDefinition.Companion.MapType> {
    override val supportedAttributeType = AttributeTypeDefinition.Companion.MapType::class
    override fun validate(definition: AttributeTypeDefinition.Companion.MapType, value: Any?): Boolean {
        val key = definition.key
        val value = definition.value
        return validators.none { it.validate(key, null).not() } &&
               validators.none { it.validate(value, null).not() }
    }
}