package dev.sayaya.handbook.usecase.validator

import dev.sayaya.handbook.domain.AttributeTypeDefinition
import dev.sayaya.handbook.domain.Document
import reactor.core.publisher.Mono
import java.util.UUID
import kotlin.reflect.KClass

sealed interface AttributeValidator<A: AttributeTypeDefinition> {
    val supportedAttributeType: KClass<A>
    @Suppress("UNCHECKED_CAST")
    fun validate(workspace: UUID, document: Document, definition: AttributeTypeDefinition, value: Any?): Mono<Boolean> = if(supportedAttributeType.isInstance(definition))
        validate(workspace, document, value, definition as A)
    else Mono.just(true)
    fun validate(workspace: UUID, document: Document, value: Any?, definition: A): Mono<Boolean>
}