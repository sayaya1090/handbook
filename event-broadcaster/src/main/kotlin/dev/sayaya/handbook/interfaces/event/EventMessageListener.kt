package dev.sayaya.handbook.interfaces.event

import dev.sayaya.handbook.usecase.Broadcaster
import org.springframework.stereotype.Component
import java.util.function.Consumer

/**
 * Kafka 등 외부 메시징 시스템에서 이벤트 메시지를 수신하는 리스너.
 * Spring Cloud Stream의 Consumer 바인딩으로 연결된다.
 */
@Component("event")
class EventMessageListener(private val broadcaster: Broadcaster) : Consumer<String> {
    override fun accept(event: String) = broadcaster.broadcast(event)
}
