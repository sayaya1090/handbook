package dev.sayaya.handbook.usecase.validator

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.type.TypeReference
import dev.sayaya.handbook.domain.AttributeTypeDefinition
import dev.sayaya.handbook.domain.Document
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class ArrayValidator(
    @Lazy private val validators: List<AttributeValidator<*>>,
    private val om: ObjectMapper
): AttributeValidator<AttributeTypeDefinition.Companion.ArrayType> {
    override val supportedAttributeType = AttributeTypeDefinition.Companion.ArrayType::class
    override fun validate(workspace: UUID, document: Document, value: Any?, definition: AttributeTypeDefinition.Companion.ArrayType): Mono<Boolean> {
        if(value==null) return Mono.just(true)
        val child = definition.type
        val list = value as? List<Any?> ?: om.convertValue(value, object : TypeReference<List<Any?>>() {})
        return Flux.fromIterable(list).flatMap { item ->
            validateItem(workspace, document, item, child)
        }.all { it }
    }
    private fun validateItem(workspace: UUID, document: Document, item: Any?, def: AttributeTypeDefinition): Mono<Boolean> = Flux.fromIterable(validators)
        .flatMap { it.validate(workspace, document, def, item) }
        .all { it }
}