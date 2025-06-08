package dev.sayaya.handbook.usecase.validator

import dev.sayaya.handbook.domain.AttributeTypeDefinition
import dev.sayaya.handbook.domain.Document
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class ValueValidator: AttributeValidator<AttributeTypeDefinition.Companion.ValueType> {
    override val supportedAttributeType = AttributeTypeDefinition.Companion.ValueType::class
    override fun validate(workspace: UUID, document: Document, value: Any?, definition: AttributeTypeDefinition.Companion.ValueType): Mono<Boolean> {
        return definition.validators.none { def ->
            def.validate(value).not()
        }.let { Mono.just(it) }
    }
}