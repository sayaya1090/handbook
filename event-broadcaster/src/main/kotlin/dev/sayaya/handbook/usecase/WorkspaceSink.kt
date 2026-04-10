package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.event.Event
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import java.io.Serializable
import java.util.concurrent.atomic.AtomicInteger

/**
 * 워크스페이스별 이벤트 Sink를 래핑하여 구독자 수를 추적하는 래퍼.
 *
 * **책임:** Reactor [Sinks.Many]를 감싸고, 구독자 카운트를 [AtomicInteger]로 관리한다.
 * 구독자가 0이 되면 [WorkspaceSinkManager]가 이 Sink를 완료(complete)하고 맵에서 제거한다.
 *
 * **주의:** 구독자 카운트 변경([incrementSubscribers]/[decrementSubscribers])은 반드시
 * [WorkspaceSinkManager]의 [ConcurrentHashMap.compute] 블록 내에서 호출되어야 원자성이 보장된다.
 */
class WorkspaceSink(
    private val sink: Sinks.Many<Event<out Serializable>>
) {
    private val subscriberCount = AtomicInteger(0)

    fun incrementSubscribers(): Int = subscriberCount.incrementAndGet()
    fun decrementSubscribers(): Int = subscriberCount.decrementAndGet()

    fun asFlux(): Flux<Event<out Serializable>> = sink.asFlux()
    fun tryEmitNext(event: Event<out Serializable>): Sinks.EmitResult = sink.tryEmitNext(event)
    fun tryEmitComplete(): Sinks.EmitResult = sink.tryEmitComplete()
}
