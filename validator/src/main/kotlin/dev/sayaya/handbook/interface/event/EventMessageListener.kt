package dev.sayaya.handbook.`interface`.event

import com.fasterxml.jackson.databind.ObjectMapper
import dev.sayaya.handbook.domain.event.DocumentEvent
import dev.sayaya.handbook.domain.event.Event
import dev.sayaya.handbook.domain.event.TypeEvent
import dev.sayaya.handbook.usecase.ValidationRequestService
import dev.sayaya.handbook.usecase.ValidatorService
import org.springframework.stereotype.Component
import java.util.function.Consumer

@Component("event")
class EventMessageListener(
    private val validator: ValidatorService,
    private val requester: ValidationRequestService,
    private val om: ObjectMapper
): Consumer<String> {
    override fun accept(event: String) = synchronized(this) {
        try {
            val event = om.readValue(event, Event::class.java)
            when {
                event is DocumentEvent && event.type!= Event.EventType.DELETE_DOCUMENT  -> validator.validate(event)
                event is TypeEvent && event.type!= Event.EventType.DELETE_TYPE          -> requester.request(event)
            }
        } catch (e: Exception) {
            throw IllegalStateException("Failed to process event: $event", e)
        }
    }
}