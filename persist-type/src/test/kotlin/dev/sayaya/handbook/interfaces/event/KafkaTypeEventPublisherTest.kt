package dev.sayaya.handbook.interfaces.event

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.domain.event.Event
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.kafka.core.KafkaTemplate
import java.time.Instant
import java.util.*

class KafkaTypeEventPublisherTest : BehaviorSpec({
    val objectMapper = JsonMapper.builder()
        .addModule(KotlinModule.Builder().build())
        .addModule(JavaTimeModule())
        .build()
    val topic = "handbook-events"
    val workspace = UUID.randomUUID()
    val type = Type(
        id = "customer",
        version = "1.0",
        effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
        expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
        description = "고객 타입",
        primitive = false,
    )

    Given("타입 생성 이벤트 발행") {
        val kafkaTemplate = mockk<KafkaTemplate<String, String>>(relaxed = true)
        val publisher = KafkaTypeEventPublisher(kafkaTemplate, objectMapper, topic)

        When("publishCreated를 호출하면") {
            publisher.publishCreated(workspace, type)

            val keySlot = slot<String>()
            val valueSlot = slot<String>()
            verify { kafkaTemplate.send(topic, capture(keySlot), capture(valueSlot)) }

            Then("Workspace UUID가 파티션 키로 사용된다") {
                keySlot.captured shouldBe workspace.toString()
            }
            Then("TYPE_CREATED 이벤트가 Kafka로 전송된다") {
                val json = objectMapper.readTree(valueSlot.captured)
                json.get("event_type").asText() shouldBe Event.EventType.TYPE_CREATED.name
                json.get("workspace").asText() shouldBe workspace.toString()
            }
            Then("페이로드에 타입 정보가 포함된다") {
                val json = objectMapper.readTree(valueSlot.captured)
                val payload = json.get("payload")
                payload.get("id").asText() shouldBe "customer"
                payload.get("version").asText() shouldBe "1.0"
            }
        }
    }

    Given("타입 삭제 이벤트 발행") {
        val kafkaTemplate = mockk<KafkaTemplate<String, String>>(relaxed = true)
        val publisher = KafkaTypeEventPublisher(kafkaTemplate, objectMapper, topic)

        When("publishDeleted를 호출하면") {
            publisher.publishDeleted(workspace, type)

            val keySlot = slot<String>()
            val valueSlot = slot<String>()
            verify { kafkaTemplate.send(topic, capture(keySlot), capture(valueSlot)) }

            Then("Workspace UUID가 파티션 키로 사용된다") {
                keySlot.captured shouldBe workspace.toString()
            }
            Then("TYPE_DELETED 이벤트가 Kafka로 전송된다") {
                val json = objectMapper.readTree(valueSlot.captured)
                json.get("event_type").asText() shouldBe Event.EventType.TYPE_DELETED.name
                json.get("workspace").asText() shouldBe workspace.toString()
            }
            Then("페이로드에 타입 정보가 포함된다") {
                val json = objectMapper.readTree(valueSlot.captured)
                val payload = json.get("payload")
                payload.get("id").asText() shouldBe "customer"
                payload.get("version").asText() shouldBe "1.0"
            }
        }
    }
})
