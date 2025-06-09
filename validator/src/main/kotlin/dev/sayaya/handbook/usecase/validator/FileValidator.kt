package dev.sayaya.handbook.usecase.validator

import dev.sayaya.handbook.domain.AttributeTypeDefinition
import dev.sayaya.handbook.domain.Document
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.UUID

// 파일 저장 구조를 결정하고 난 다음 구현하자
@Component
class FileValidator: AttributeValidator<AttributeTypeDefinition.Companion.FileType> {
    override val supportedAttributeType = AttributeTypeDefinition.Companion.FileType::class
    override fun validate(workspace: UUID, document: Document, value: Any?, definition: AttributeTypeDefinition.Companion.FileType): Mono<Boolean> {
        if(value==null) return Mono.just(true)
        return Mono.just(true)
    }
}