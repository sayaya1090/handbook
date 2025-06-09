package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.event.Event
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class WorkspaceSinkManager {
    private val workspaceSinks: ConcurrentHashMap<UUID, WorkspaceSink> = ConcurrentHashMap()
    fun tryEmitNext(event: Event<*, *>) {
        workspaceSinks[event.workspace]?.tryEmitNext(event)
    }
    fun listen(workspace: UUID): Flux<Event<*, *>> {
        val workspaceSink = workspaceSinks.computeIfAbsent(workspace) {
            WorkspaceSink(Sinks.many().replay().limit(Duration.ofMillis(10)))
        }
        return workspaceSink.asFlux().doFinally {
            workspaceSinks.computeIfPresent(workspace) { _, sinkInMap ->
                if (sinkInMap.releaseSubscriptionAndGetCount() <= 0) null
                else sinkInMap
            }
        }
    }
}