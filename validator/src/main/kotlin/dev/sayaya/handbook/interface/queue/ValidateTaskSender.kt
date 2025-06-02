package dev.sayaya.handbook.`interface`.queue

import com.fasterxml.jackson.databind.ObjectMapper
import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.event.DocumentEvent
import dev.sayaya.handbook.domain.event.Event
import dev.sayaya.handbook.usecase.TaskQueue
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.util.UUID
import java.util.function.Supplier

@Component
class ValidateTaskSender(private val om: ObjectMapper): TaskQueue {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val buffer: Sinks.Many<DocumentEvent> = Sinks.many().unicast().onBackpressureBuffer()
    @Bean("validate-request") fun validateRequest(): Supplier<Flux<String>> = Supplier {
        buffer.asFlux().mapNotNull { msg -> om.writeValueAsString(msg) }.onErrorResume { e ->
            logger.error("Error occurred while processing buffer", e)
            Flux.empty()
        }
    }
    override fun publish(workspace: UUID, document: Document): Mono<Void> = synchronized(buffer) {
        val event = DocumentEvent(
            id=UUID.randomUUID(),
            workspace=workspace,
            type = Event.EventType.UPDATE_DOCUMENT,
            param=document
        )
        if (buffer.tryEmitNext(event).isSuccess) Mono.empty()
        else Mono.error(IllegalStateException("Failed to emit event for document: $document"))
    }
}