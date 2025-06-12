package dev.sayaya.handbook.`interface`.api

import com.fasterxml.jackson.databind.ObjectMapper
import dev.sayaya.handbook.usecase.Broadcaster
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.time.Duration
import java.util.UUID

/*
  HTTP 1.1을 사용할 경우 중간에 연결이 끊어질 수 있다. 끊어짐 방지를 위해 주기적으로 핑을 날린다.
 */
@RestController
class MessageController(private val svc: Broadcaster, private val om: ObjectMapper) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val ping = Flux.interval(Duration.ofSeconds(10)).map { ServerSentEvent.builder<String>().comment("ping").build() }
    @GetMapping("/workspace/{workspace}/messages", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun messages(@PathVariable workspace: UUID): Flux<ServerSentEvent<String>> {
        logger.info("Messages requested")
        return Flux.merge(
            svc.listen(workspace).map {
                ServerSentEvent.builder<String>().id(it.id.toString()).event(it.type.toString()).data(om.writeValueAsString(it.param)).build()
            }, ping
        ).doOnCancel   { logger.info("Client disconnected") }
         .doOnComplete { logger.info("Complete") }
         .doOnError    { logger.error("Error occurred: ${it.message}") }
    }
}