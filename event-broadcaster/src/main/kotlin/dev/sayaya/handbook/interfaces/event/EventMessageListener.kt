package dev.sayaya.handbook.interfaces.event

import dev.sayaya.handbook.usecase.Broadcaster
import org.springframework.stereotype.Component
import java.util.function.Consumer

/**
 * Kafka 이벤트 메시지를 수신하는 Spring Cloud Stream 리스너.
 *
 * **책임:** Spring Cloud Stream의 Consumer 바인딩("event")으로 Kafka 토픽에서
 * 이벤트 메시지(JSON 문자열)를 수신하고, [Broadcaster]에 전달하여 브로드캐스트를 트리거한다.
 *
 * **의존관계:**
 * - [Broadcaster] — 수신된 이벤트의 역직렬화 및 워크스페이스별 분배
 */
@Component("event")
class EventMessageListener(private val broadcaster: Broadcaster) : Consumer<String> {
    override fun accept(event: String) = broadcaster.broadcast(event)
}
