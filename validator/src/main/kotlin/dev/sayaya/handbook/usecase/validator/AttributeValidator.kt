package dev.sayaya.handbook.usecase.validator

import dev.sayaya.handbook.domain.AttributeTypeDefinition
import kotlin.reflect.KClass

sealed interface AttributeValidator<A: AttributeTypeDefinition> {
    val supportedAttributeType: KClass<A>
    @Suppress("UNCHECKED_CAST")
    fun validate(definition: AttributeTypeDefinition, value: Any?): Boolean = if(supportedAttributeType.isInstance(definition)) validate(definition as A, value)
    else true
    fun validate(definition: A, value: Any?): Boolean
}