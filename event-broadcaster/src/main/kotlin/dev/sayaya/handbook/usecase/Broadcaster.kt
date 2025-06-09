package dev.sayaya.handbook.usecase

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import dev.sayaya.handbook.domain.event.Event
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class Broadcaster(private val om: ObjectMapper) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val sink: Sinks.Many<Event<*, *>> = Sinks.many().replay().limit(0)
    private val workspaceSinks: ConcurrentHashMap<UUID, Sinks.Many<Event<*, *>>> = ConcurrentHashMap()
    init {
        sink.asFlux()
            .doOnNext { event ->
                workspaceSinks.computeIfAbsent(event.workspace) {
                    Sinks.many().replay().limit(0)
                }.tryEmitNext(event)
            }.subscribe()
    }

    fun broadcast(event: String) {
        val emit = sink.tryEmitNext(om.readValue(event, object : TypeReference<Event<*, *>>() {}))
        logger.info(emit.toString())
    }
    fun listen(workspace: UUID): Flux<Event<*, *>> = workspaceSinks.computeIfAbsent(workspace) {
        Sinks.many().replay().limit(0)
    }.asFlux()
}