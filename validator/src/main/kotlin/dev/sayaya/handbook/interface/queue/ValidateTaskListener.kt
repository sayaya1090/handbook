package dev.sayaya.handbook.`interface`.queue

import com.fasterxml.jackson.databind.ObjectMapper
import dev.sayaya.handbook.domain.event.Event
import dev.sayaya.handbook.`interface`.event.EventProcessor
import org.springframework.stereotype.Component
import java.util.function.Consumer

@Component("request-listener")
class ValidateTaskListener(
    private val om: ObjectMapper,
    private val processor: EventProcessor
): Consumer<String> {
    override fun accept(event: String): Unit = synchronized(this) {
        val event = om.readValue(event, Event::class.java)
        processor.processEvent(event).subscribe()
    }
}