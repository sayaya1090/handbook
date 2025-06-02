package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.event.DocumentEvent
import org.springframework.stereotype.Service

@Service
class ValidatorService(private val type: TypeRepository) {
    fun validate(event: DocumentEvent) {
        val document = event.param
        type.find(event.workspace, event.param.type, "").subscribe()
    }
}