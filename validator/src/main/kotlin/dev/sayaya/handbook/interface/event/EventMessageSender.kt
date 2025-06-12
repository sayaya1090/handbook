package dev.sayaya.handbook.`interface`.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.f4b6a3.ulid.Ulid
import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.event.DocumentEvent
import dev.sayaya.handbook.domain.event.Event
import dev.sayaya.handbook.usecase.ExternalService
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
    @Bean("event-produce") fun event(): Supplier<Flux<String>> = Supplier {
        buffer.asFlux().mapNotNull { msg -> om.writeValueAsString(msg) }.onErrorResume { e ->
            logger.error("Error occurred while processing buffer", e)
            Flux.empty()
        }
    }
    override fun publish(workspace: UUID, document: Document): Mono<Void> = Mono.fromCallable {
        DocumentEvent(
            id = Ulid.fast().toUuid(),
            workspace = workspace,
            type = Event.EventType.UPDATE_DOCUMENT,
            param = document
        )
    }.flatMap { evt ->
        val emitResult = synchronized(buffer) {
            buffer.tryEmitNext(evt)
        }
        if (emitResult.isSuccess) {
            logger.info("Successfully emitted event with ID: {}", evt.id)
            Mono.empty()
        } else {
            logger.error("Failed to emit event with ID: {}. Reason: {}", evt.id, emitResult)
            Mono.error(RuntimeException("Failed to emit event. Reason: $emitResult"))
        }
    }
}