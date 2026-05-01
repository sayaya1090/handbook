package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.event.PresencePayload
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.web.reactive.server.WebTestClient
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.util.*
import java.util.concurrent.CompletableFuture

class PresenceControllerTest : DescribeSpec({
    val kafkaTemplate = mockk<KafkaTemplate<String, String>>()
    val objectMapper = jacksonObjectMapper()
    val controller = PresenceController(kafkaTemplate, objectMapper)
    val client = WebTestClient.bindToController(controller).build()

    describe("PresenceController") {
        it("publish: 프레즌스 이벤트를 Kafka로 발행한다") {
            val workspaceId = UUID.randomUUID()
            val payload = PresencePayload("user-1", "Alice", "customer")
            
            every { kafkaTemplate.send(any(), any(), any()) } returns mockk {
                every { toCompletableFuture() } returns CompletableFuture.completedFuture(mockk())
            }

            client.post()
                .uri("/workspace/$workspaceId/presence")
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk

            verify { kafkaTemplate.send("handbook-events", workspaceId.toString(), any()) }
        }
    }
})
