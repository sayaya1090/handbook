package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.AttributeTypeDefinition
import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.domain.event.DocumentEvent
import dev.sayaya.handbook.usecase.validator.AttributeValidator
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class ValidatorService(
    private val typeRepo: TypeRepository,
    private val taskRepo: ValidationTaskRepository,
    private val validators: List<AttributeValidator<*>>
) {
    fun validate(event: DocumentEvent): Mono<Void> {
        val document = event.param
        return typeRepo.find(event.workspace, document.type, document.effectDateTime, document.expireDateTime)
            .flatMap { document.validate(it) }
            .flatMap { taskRepo.save(event.workspace, document, it) }
    }
    private fun Document.validate(type: Type): Mono<Map<String, Boolean>> = Flux.fromIterable(type.attributes).flatMap { attr ->
        val key = attr.name
        val value = data[key]
        val def = attr.type
        Mono.just(key to isValid(def, value))
    }.collectMap({ it.first }, { it.second })

    private fun isValid(def: AttributeTypeDefinition, value: Any?): Boolean = validators.all { validator -> validator.validate(def, value) }
}