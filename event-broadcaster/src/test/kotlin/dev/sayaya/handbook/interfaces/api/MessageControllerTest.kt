package dev.sayaya.handbook.interfaces.api

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.module.kotlin.KotlinModule
import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.event.DocumentEvent
import dev.sayaya.handbook.domain.event.Event
import dev.sayaya.handbook.usecase.Broadcaster
import dev.sayaya.handbook.usecase.WorkspaceSinkManager
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import reactor.test.StepVerifier
import java.time.Instant
import java.util.*

class MessageControllerTest : DescribeSpec({

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

    describe("MessageController는") {

        it("이벤트를 SSE 형식으로 스트리밍한다") {
            val sinkManager = WorkspaceSinkManager()
            val broadcaster = Broadcaster(objectMapper, sinkManager)
            val controller = MessageController(broadcaster, objectMapper)
            val event = sampleEvent()

            StepVerifier.create(controller.messages(workspace).take(1))
                .then { broadcaster.broadcast(objectMapper.writeValueAsString(event)) }
                .assertNext { sse ->
                    sse.id() shouldBe event.id.toString()
                    sse.event() shouldBe "DOCUMENT_CREATED"
                    sse.data() shouldNotBe null
                }
                .verifyComplete()
        }
    }
})
