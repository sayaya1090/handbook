package dev.sayaya.handbook.usecase.validator

import dev.sayaya.handbook.domain.AttributeTypeDefinition
import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.usecase.DocumentRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class DocumentValidator(
    private val repo: DocumentRepository
): AttributeValidator<AttributeTypeDefinition.Companion.DocumentType> {
    override val supportedAttributeType = AttributeTypeDefinition.Companion.DocumentType::class
    override fun validate(workspace: UUID, document: Document, value: Any?, definition: AttributeTypeDefinition.Companion.DocumentType): Mono<Boolean> {
        if(value==null) return Mono.just(true)
        if(value !is String) return Mono.just(false)
        val type = definition.referencedType
        val edt = document.effectDateTime
        val xdt = document.expireDateTime
        return repo.findByType(workspace, type, edt, xdt).map { it.serial }.hasElement(value)
    }
}