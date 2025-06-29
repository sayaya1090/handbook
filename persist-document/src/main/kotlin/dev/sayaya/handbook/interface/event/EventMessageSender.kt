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
    override fun update(principal: Principal, workspace: UUID, documents: List<Document>): Mono<Void> = publish(workspace, documents, Event.EventType.UPDATE_DOCUMENT)
    override fun delete(principal: Principal, workspace: UUID, documents: List<Document>): Mono<Void> = publish(workspace, documents, Event.EventType.DELETE_DOCUMENT)
    private fun publish(workspace: UUID, documents: List<Document>, type: Event.EventType): Mono<Void> = synchronized(buffer) {
        val failures = documents.stream().map { document ->
            DocumentEvent(
                id = Ulid.fast().toUuid(),
                workspace = workspace,
                type = type,
                param = document
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