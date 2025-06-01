package dev.sayaya.handbook.`interface`.event

import dev.sayaya.handbook.usecase.Broadcaster
import org.springframework.stereotype.Component
import java.util.function.Consumer

@Component("handbook")
class EventMessageListener(private val emitter: Broadcaster): Consumer<String> {
    override fun accept(event: String) = emitter.broadcast(event)
}