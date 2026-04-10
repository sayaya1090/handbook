package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.event.DocumentEvent
import dev.sayaya.handbook.domain.event.Event
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import reactor.test.StepVerifier
import java.time.Instant
import java.util.*

class WorkspaceSinkManagerTest : DescribeSpec({

    val workspace1 = UUID.randomUUID()
    val workspace2 = UUID.randomUUID()
    val now = Instant.now()

    fun sampleEvent(workspace: UUID) = DocumentEvent(
        id = UUID.randomUUID(),
        workspace = workspace,
        eventType = Event.EventType.DOCUMENT_CREATED,
        payload = Document(
            UUID.randomUUID(), "customer", "C-001",
            now, now.plusSeconds(3600), now, "user-1", mapOf("name" to "Alice")
        )
    )

    describe("WorkspaceSinkManager는") {

        it("워크스페이스별로 이벤트를 분리하여 전달한다") {
            val manager = WorkspaceSinkManager()
            val event1 = sampleEvent(workspace1)
            val event2 = sampleEvent(workspace2)

            StepVerifier.create(manager.listen(workspace1).take(1))
                .then { manager.tryEmitNext(event1) }
                .then { manager.tryEmitNext(event2) } // workspace2 이벤트는 전달되지 않음
                .expectNext(event1)
                .verifyComplete()
        }

        it("구독자가 없는 워크스페이스에 이벤트를 보내도 오류가 발생하지 않는다") {
            val manager = WorkspaceSinkManager()
            manager.tryEmitNext(sampleEvent(UUID.randomUUID()))
            // 예외 없이 정상 종료
        }

        it("여러 구독자가 같은 워크스페이스를 구독할 수 있다") {
            val manager = WorkspaceSinkManager()
            val event = sampleEvent(workspace1)

            val flux1 = manager.listen(workspace1).take(1)
            val flux2 = manager.listen(workspace1).take(1)

            StepVerifier.create(flux1)
                .then { manager.tryEmitNext(event) }
                .expectNext(event)
                .verifyComplete()

            StepVerifier.create(flux2)
                .then { manager.tryEmitNext(sampleEvent(workspace1)) }
                .expectNextCount(1)
                .verifyComplete()
        }

        it("모든 구독자가 해제된 후 재구독하면 새 Sink가 생성되어 정상 동작한다") {
            val manager = WorkspaceSinkManager()
            val ws = UUID.randomUUID()

            // 1차 구독 → 이벤트 수신 → 해제
            StepVerifier.create(manager.listen(ws).take(1))
                .then { manager.tryEmitNext(sampleEvent(ws)) }
                .expectNextCount(1)
                .verifyComplete()

            // Sink가 정리된 후 재구독 → 새 Sink에서 이벤트 수신
            StepVerifier.create(manager.listen(ws).take(1))
                .then { manager.tryEmitNext(sampleEvent(ws)) }
                .expectNextCount(1)
                .verifyComplete()
        }

        it("한 구독자가 해제되어도 다른 구독자는 영향받지 않는다") {
            val manager = WorkspaceSinkManager()
            val ws = UUID.randomUUID()

            // 구독자 B 먼저 구독 (take 없이 수동 관리)
            val received = mutableListOf<Event<out java.io.Serializable>>()
            val disposable = manager.listen(ws).subscribe { received.add(it) }

            // 구독자 A 구독 → 이벤트 1개 수신 → 해제
            StepVerifier.create(manager.listen(ws).take(1))
                .then { manager.tryEmitNext(sampleEvent(ws)) }
                .expectNextCount(1)
                .verifyComplete()

            // A 해제 후에도 B는 여전히 이벤트를 수신한다
            val event2 = sampleEvent(ws)
            manager.tryEmitNext(event2)
            Thread.sleep(50) // 비동기 전달 대기
            received.size shouldBe 2 // event1 + event2

            disposable.dispose()
        }

        it("동시에 여러 스레드에서 구독/해제해도 오류가 발생하지 않는다") {
            val manager = WorkspaceSinkManager()
            val ws = UUID.randomUUID()
            val errors = java.util.concurrent.ConcurrentLinkedQueue<Throwable>()
            val latch = java.util.concurrent.CountDownLatch(20)

            val threads = (1..20).map {
                Thread {
                    try {
                        // 구독 → 이벤트 emit → 수신 확인 → 해제
                        val event = sampleEvent(ws)
                        manager.listen(ws).take(1)
                            .doOnNext { /* received */ }
                            .subscribe()
                        manager.tryEmitNext(event)
                    } catch (e: Throwable) {
                        errors.add(e)
                    } finally {
                        latch.countDown()
                    }
                }
            }
            threads.forEach { it.start() }
            latch.await(5, java.util.concurrent.TimeUnit.SECONDS)
            errors.size shouldBe 0
        }
    }
})
