package dev.sayaya.handbook.interfaces.event

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.event.DocumentEvent
import dev.sayaya.handbook.domain.event.Event
import dev.sayaya.handbook.usecase.Broadcaster
import dev.sayaya.handbook.usecase.WorkspaceSinkManager
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

class EventMessageListenerTest : DescribeSpec({

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

    describe("EventMessageListener는") {

        it("JSON 문자열을 받아 Broadcaster로 위임한다") {
            val sinkManager = WorkspaceSinkManager()
            val broadcaster = Broadcaster(objectMapper, sinkManager)
            val listener = EventMessageListener(broadcaster)

            val event = DocumentEvent(
                UUID.randomUUID(), workspace, Event.EventType.DOCUMENT_CREATED,
                Document.create(UUID.randomUUID().toString(), "order", "O-100", now.toEpochMilli().toDouble(), now.plusSeconds(3600).toEpochMilli().toDouble(), now.toEpochMilli().toDouble(), "user-1", null)
            )
            val json = objectMapper.writeValueAsString(event)

            StepVerifier.create(broadcaster.listen(workspace).take(1))
                .then { listener.accept(json) }
                .assertNext { received ->
                    received.eventType shouldBe Event.EventType.DOCUMENT_CREATED
                    received.workspace shouldBe workspace
                }
                .verifyComplete()
        }
    }
})
