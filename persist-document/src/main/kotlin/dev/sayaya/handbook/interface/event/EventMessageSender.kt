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
    @Bean("event") fun event(): Supplier<Flux<String>> = Supplier {
        buffer.asFlux().mapNotNull { msg -> om.writeValueAsString(msg) }.onErrorResume { e ->
            logger.error("Error occurred while processing buffer", e)
            Flux.empty()
        }
    }
    override fun publish(principal: Principal, workspace: UUID, documents: Map<ExternalService.DocumentKey, Document?>): Mono<Void> = synchronized(buffer) {
        val failures = documents.entries.stream().map {
            (key, document) ->
            DocumentEvent(
                id = Ulid.fast().toUuid(),
                workspace = workspace,
                type = if(document!=null) Event.EventType.UPDATE_DOCUMENT else Event.EventType.DELETE_DOCUMENT,
                param = document ?: Document(
                    id = Ulid.fast().toUuid(),
                    type = key.type,
                    serial = key.serial,
                    effectDateTime = Instant.now(),
                    expireDateTime = Instant.now(),
                    createDateTime = Instant.now(),
                    creator = null,
                    data = emptyMap(),
                    validations = null
                )
            )
        }.map(buffer::tryEmitNext).filter { it.isFailure }.toList()

        if (failures.isNotEmpty()) {
            logger.error("Failed to emit events: ${failures.size} errors occurred")
            Mono.error(RuntimeException("Failed to emit events: ${failures.size} errors occurred"))
        } else {
            logger.info("Successfully emitted ${documents.size} events")
            Mono.empty()
        }
    }
}