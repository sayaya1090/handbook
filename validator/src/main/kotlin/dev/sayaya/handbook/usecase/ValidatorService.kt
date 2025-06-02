package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.event.DocumentEvent
import org.springframework.stereotype.Service

@Service
class ValidatorService {
    fun validate(event: DocumentEvent) {
        println(event)
    }
}