package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.event.Event
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import java.util.concurrent.atomic.AtomicInteger

class WorkspaceSink (
    private val sink: Sinks.Many<Event<*, *>>
): Sinks.Many<Event<*, *>> by sink {
    private val activeSubscribers: AtomicInteger = AtomicInteger(0)
    override fun asFlux(): Flux<Event<*, *>> {
        activeSubscribers.incrementAndGet()
        return sink.asFlux()
    }
    fun releaseSubscriptionAndGetCount(): Int {
        val currentCount = activeSubscribers.decrementAndGet()
        if (currentCount <= 0 && sink.currentSubscriberCount() == 0) sink.tryEmitComplete()
        return currentCount
    }
}