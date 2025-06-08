package dev.sayaya.handbook.usecase.validator

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import dev.sayaya.handbook.domain.AttributeTypeDefinition
import dev.sayaya.handbook.domain.Document
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class MapValidator(
    @Lazy private val validators: List<AttributeValidator<*>>,
    private val om: ObjectMapper
): AttributeValidator<AttributeTypeDefinition.Companion.MapType> {
    override val supportedAttributeType = AttributeTypeDefinition.Companion.MapType::class
    override fun validate(workspace: UUID, document: Document, value: Any?, definition: AttributeTypeDefinition.Companion.MapType): Mono<Boolean> {
        val key = definition.key
        val value = definition.value
        val map = value as? Map<Any?, Any?> ?: om.convertValue(value, object : TypeReference<Map<Any?, Any?>>() {})
        val validateKeys = Flux.fromIterable(map.keys)
            .flatMap { validateItem(workspace, document, it, key) }
            .all { it }
        val validateValues = Flux.fromIterable(map.values)
            .flatMap { validateItem(workspace, document, it, value) }
            .all { it }
        return validateKeys.zipWith(validateValues) { keysValid, valuesValid ->
            keysValid && valuesValid
        }
    }
    private fun validateItem(workspace: UUID, document: Document, item: Any?, def: AttributeTypeDefinition): Mono<Boolean> = Flux.fromIterable(validators)
        .flatMap { it.validate(workspace, document, def, item) }
        .all { it }
}