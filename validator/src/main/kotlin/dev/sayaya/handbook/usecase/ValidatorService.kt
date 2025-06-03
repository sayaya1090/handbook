package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.event.DocumentEvent
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ValidatorService(private val type: TypeRepository) {
    fun validate(event: DocumentEvent): Mono<Void> {
        val document = event.param
        return type.find(event.workspace, event.param.type, "")
    }
}