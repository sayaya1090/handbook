package dev.sayaya.handbook.`interface`.queue

import com.fasterxml.jackson.databind.ObjectMapper
import dev.sayaya.handbook.domain.event.DocumentEvent
import dev.sayaya.handbook.domain.event.Event
import dev.sayaya.handbook.usecase.ValidatorService
import org.springframework.stereotype.Component
import java.util.function.Consumer

@Component("request-listener")
class ValidateTaskListener(
    private val validator: ValidatorService,
    private val om: ObjectMapper
): Consumer<String> {
    override fun accept(event: String) = synchronized(this) {
        try {
            val event = om.readValue(event, Event::class.java)
            when {
                event is DocumentEvent && event.type!= Event.EventType.DELETE_DOCUMENT  -> validator.validate(event)
            }
        } catch (e: Exception) {
            throw IllegalStateException("Failed to process event: $event", e)
        }
    }
}