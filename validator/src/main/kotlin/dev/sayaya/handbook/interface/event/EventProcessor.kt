package dev.sayaya.handbook.`interface`.event

import dev.sayaya.handbook.domain.event.DocumentEvent
import dev.sayaya.handbook.domain.event.Event
import dev.sayaya.handbook.domain.event.TypeEvent
import dev.sayaya.handbook.usecase.ValidationRequestService
import dev.sayaya.handbook.usecase.ValidatorService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Component
class EventProcessor (
    private val validator: ValidatorService,
    private val requester: ValidationRequestService
) {
    @Transactional
    fun processEvent(event: Event<*, *>): Mono<Void> = when {
        event is DocumentEvent &&
                 event.type!= Event.EventType.DELETE_DOCUMENT &&
                 event.type!=Event.EventType.VALIDATE_DOCUMENT -> validator.validate(event)
        event is TypeEvent && event.type!= Event.EventType.DELETE_TYPE          -> requester.request(event)
        else -> Mono.error { IllegalArgumentException("unsupported event type: ${event.type}") }
    }
}
