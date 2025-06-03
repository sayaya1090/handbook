package dev.sayaya.handbook.usecase.validator

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.type.TypeReference
import dev.sayaya.handbook.domain.AttributeTypeDefinition
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class ArrayValidator(
    @Lazy private val validators: List<AttributeValidator<*>>,
    private val om: ObjectMapper
): AttributeValidator<AttributeTypeDefinition.Companion.ArrayType> {
    override val supportedAttributeType = AttributeTypeDefinition.Companion.ArrayType::class
    override fun validate(value: Any?, definition: AttributeTypeDefinition.Companion.ArrayType): Boolean {
        if(value==null) return true
        val child = definition.type
        val list = value as? List<Any?> ?: om.convertValue(value, object : TypeReference<List<Any?>>() {})
        return list.none { item ->
            validators.none { it.validate(child, item).not() }
        }
    }
}