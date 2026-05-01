package dev.sayaya.handbook.usecase

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.event.DocumentEvent
import dev.sayaya.handbook.domain.event.Event
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import reactor.test.StepVerifier
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import java.time.Instant
import java.util.*

class BroadcasterTest : DescribeSpec({

    val objectMapper = JsonMapper.builder()
        .disable(DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .changeDefaultVisibility { it.withVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY) }
        .addModule(KotlinModule.Builder().withReflectionCacheSize(512).build())
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build()

    val workspace = UUID.randomUUID()
    val now = Instant.now()

    fun sampleEvent() = DocumentEvent(
        id = UUID.randomUUID(),
        workspace = workspace,
        eventType = Event.EventType.DOCUMENT_CREATED,
        payload = Document(
            UUID.randomUUID(), "customer", "C-001",
            now, now.plusSeconds(3600), now, "user-1", mapOf("name" to "Alice")
        )
    )

    describe("Broadcaster는") {

        it("JSON 문자열을 역직렬화하여 워크스페이스별로 브로드캐스트한다") {
            val sinkManager = WorkspaceSinkManager()
            val broadcaster = Broadcaster(objectMapper, sinkManager)
            val event = sampleEvent()
            val json = objectMapper.writeValueAsString(event)

            StepVerifier.create(broadcaster.listen(workspace).take(1))
                .then { broadcaster.broadcast(json) }
                .assertNext { received ->
                    received.id shouldBe event.id
                    received.eventType shouldBe Event.EventType.DOCUMENT_CREATED
                    received.workspace shouldBe workspace
                }
                .verifyComplete()
        }

        // UC-EB5: 다중 구독자 브로드캐스트
        it("동일 워크스페이스의 다중 구독자에게 이벤트를 동시에 전달한다") {
            val sinkManager = WorkspaceSinkManager()
            val broadcaster = Broadcaster(objectMapper, sinkManager)
            val event = sampleEvent()
            val json = objectMapper.writeValueAsString(event)

            val flux1 = broadcaster.listen(workspace).take(1)
            val flux2 = broadcaster.listen(workspace).take(1)

            // 구독자 1 검증
            StepVerifier.create(flux1)
                .then { broadcaster.broadcast(json) }
                .assertNext { received ->
                    received.id shouldBe event.id
                    received.eventType shouldBe Event.EventType.DOCUMENT_CREATED
                    received.workspace shouldBe workspace
                }
                .verifyComplete()

            // 구독자 2도 새 이벤트를 수신할 수 있다
            val event2 = sampleEvent()
            val json2 = objectMapper.writeValueAsString(event2)
            StepVerifier.create(flux2)
                .then { broadcaster.broadcast(json2) }
                .assertNext { received ->
                    received.id shouldBe event2.id
                    received.workspace shouldBe workspace
                }
                .verifyComplete()
        }

        it("다른 워크스페이스의 구독자에게는 이벤트를 전달하지 않는다") {
            val sinkManager = WorkspaceSinkManager()
            val broadcaster = Broadcaster(objectMapper, sinkManager)
            val otherWorkspace = UUID.randomUUID()
            val event = sampleEvent()
            val json = objectMapper.writeValueAsString(event)

            StepVerifier.create(broadcaster.listen(otherWorkspace).take(1))
                .then { broadcaster.broadcast(json) }
                .expectTimeout(java.time.Duration.ofMillis(500))
                .verify()
        }
    }
})
