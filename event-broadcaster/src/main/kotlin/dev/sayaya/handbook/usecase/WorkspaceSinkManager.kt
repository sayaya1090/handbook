package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.event.Event
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import java.io.Serializable
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 워크스페이스별 이벤트 Sink를 관리한다.
 *
 * 구독자 등록/해제와 Sink 생성/제거를 [ConcurrentHashMap.compute]로
 * 원자적으로 처리하여 경합 조건을 방지한다.
 *
 * - 첫 구독자 진입 시 Sink를 lazy 생성한다.
 * - 마지막 구독자 해제 시 Sink를 완료(complete)하고 맵에서 제거한다.
 * - 구독자가 없는 워크스페이스에 이벤트가 도착하면 무시한다.
 */
class WorkspaceSinkManager {
    private val workspaceSinks = ConcurrentHashMap<UUID, WorkspaceSink>()

    fun tryEmitNext(event: Event<out Serializable>) {
        workspaceSinks[event.workspace]?.tryEmitNext(event)
    }

    fun listen(workspace: UUID): Flux<Event<out Serializable>> = Flux.defer {
        val sink = workspaceSinks.compute(workspace) { _, existing ->
            (existing ?: WorkspaceSink(Sinks.many().replay().limit(Duration.ofMillis(10))))
                .also { it.incrementSubscribers() }
        }!!
        sink.asFlux().doFinally {
            workspaceSinks.compute(workspace) { _, current ->
                when {
                    current == null -> null
                    current.decrementSubscribers() <= 0 -> {
                        current.tryEmitComplete()
                        null
                    }
                    else -> current
                }
            }
        }
    }
}
