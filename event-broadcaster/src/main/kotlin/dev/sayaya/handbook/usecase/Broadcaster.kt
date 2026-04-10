package dev.sayaya.handbook.usecase

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import dev.sayaya.handbook.domain.event.Event
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import java.io.Serializable
import java.time.Duration
import java.util.*

/**
 * 도메인 이벤트를 수신하여 워크스페이스별로 브로드캐스트하는 서비스.
 *
 * Kafka 등 외부 메시징 시스템에서 JSON 문자열로 이벤트를 수신하고,
 * 역직렬화하여 워크스페이스별 Sink로 분배한다.
 */
class Broadcaster(
    private val objectMapper: ObjectMapper,
    private val sinkManager: WorkspaceSinkManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val sink: Sinks.Many<Event<out Serializable>> = Sinks.many().replay().limit(Duration.ofMillis(10))

    init {
        sink.asFlux()
            .doOnNext { event -> sinkManager.tryEmitNext(event) }
            .subscribe()
    }

    fun broadcast(event: String) {
        val parsed = objectMapper.readValue(event, object : TypeReference<Event<out Serializable>>() {})
        val result = sink.tryEmitNext(parsed)
        logger.info("Broadcast result: {}", result)
    }

    fun listen(workspace: UUID): Flux<Event<out Serializable>> = sinkManager.listen(workspace)
}
