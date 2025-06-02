package dev.sayaya.handbook.`interface`.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.f4b6a3.ulid.Ulid
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.domain.event.TypeEvent
import dev.sayaya.handbook.domain.event.Event
import dev.sayaya.handbook.usecase.type.ExternalService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.security.Principal
import java.time.Instant
import java.util.UUID
import java.util.function.Supplier

@Component
class EventMessageSender(private val om: ObjectMapper): ExternalService {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val buffer: Sinks.Many<Event<*, *>> = Sinks.many().unicast().onBackpressureBuffer()
    @Bean("event") fun event(): Supplier<Flux<String>> = Supplier {
        buffer.asFlux().mapNotNull { msg -> om.writeValueAsString(msg) }.onErrorResume { e ->
            logger.error("Error occurred while processing buffer", e)
            Flux.empty()
        }
    }
    override fun publish(principal: Principal, workspace: UUID, types: Map<ExternalService.TypeKey, Type?>): Mono<Void> = synchronized(buffer) {
        val failures = types.entries.stream().map {
            (key, type) ->
            TypeEvent(
                id = Ulid.fast().toUuid(),
                workspace = workspace,
                type = if(type!=null) Event.EventType.UPDATE_TYPE else Event.EventType.DELETE_TYPE,
                param = type ?: Type (
                    id = key.id,
                    version = key.version,
                    effectDateTime = Instant.now(),
                    expireDateTime = Instant.now(),
                    description = null,
                    primitive = false,
                    x = 0u, y= 0u, width = 1u, height = 1u,
                )
            )
        }.map(buffer::tryEmitNext).filter { it.isFailure }.toList()

        if (failures.isNotEmpty()) {
            logger.error("Failed to emit events: ${failures.size} errors occurred")
            Mono.error(RuntimeException("Failed to emit events: ${failures.size} errors occurred"))
        } else {
            logger.info("Successfully emitted ${types.size} events")
            Mono.empty()
        }
    }
}