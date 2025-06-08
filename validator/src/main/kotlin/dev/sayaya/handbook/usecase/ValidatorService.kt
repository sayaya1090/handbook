package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.AttributeTypeDefinition
import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.domain.event.DocumentEvent
import dev.sayaya.handbook.usecase.validator.AttributeValidator
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class ValidatorService(
    private val typeRepo: TypeRepository,
    private val taskRepo: ValidationTaskRepository,
    private val validators: List<AttributeValidator<*>>
) {
    fun validate(event: DocumentEvent): Mono<Void> {
        val document = event.param
        return typeRepo.find(event.workspace, document.type, document.effectDateTime, document.expireDateTime)
            .flatMap { document.validate(event.workspace, it) }
            .flatMap { taskRepo.save(event.workspace, document, it) }
    }
    private fun Document.validate(workspace: UUID, type: Type): Mono<Map<String, Boolean>> = Flux.fromIterable(type.attributes).flatMap { attr ->
        val key = attr.name
        val value = data[key]
        val def = attr.type
        val chkNullable = attr.nullable || value!=null
        if(!chkNullable) Mono.just(key to false)
        else isValid(workspace, this, def, value).map {
            key to it
        }
    }.collectMap({ it.first }, { it.second })

    private fun isValid(workspace: UUID, document: Document, def: AttributeTypeDefinition, value: Any?): Mono<Boolean> = Flux.fromIterable(validators)
        .flatMap { validator -> validator.validate(workspace, document, def, value) }
        .all { validationResult -> validationResult }
}