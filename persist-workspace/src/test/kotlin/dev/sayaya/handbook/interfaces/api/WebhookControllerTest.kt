package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Webhook
import dev.sayaya.handbook.usecase.WebhookService
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

class WebhookControllerTest : BehaviorSpec({
    val service = mockk<WebhookService>()
    val controller = WebhookController(service)
    val client = WebTestClient.bindToController(controller).build()
    val workspace = UUID.randomUUID()

    Given("웹훅 등록 API") {
        val webhook = Webhook(
            id = UUID.randomUUID(),
            workspace = workspace,
            url = "https://example.com/hook",
            events = listOf("DOCUMENT_CREATED"),
            active = true,
            createdAt = Instant.now(),
        )
        every { service.register(any()) } returns Mono.just(webhook)

        When("POST /workspace/{workspace}/webhooks를 호출하면") {
            Then("201 Created가 반환된다") {
                client.post()
                    .uri("/workspace/$workspace/webhooks")
                    .header("Content-Type", "application/vnd.sayaya.handbook.v1+json")
                    .bodyValue(WebhookController.RegisterWebhookRequest("https://example.com/hook", listOf("DOCUMENT_CREATED")))
                    .exchange()
                    .expectStatus().isCreated
            }
        }
    }

    Given("웹훅 목록 조회 API") {
        val webhooks = listOf(
            Webhook(UUID.randomUUID(), workspace, "https://example.com/hook1", listOf("DOCUMENT_CREATED"), true, Instant.now()),
            Webhook(UUID.randomUUID(), workspace, "https://example.com/hook2", emptyList(), true, Instant.now()),
        )
        every { service.list(workspace) } returns Flux.fromIterable(webhooks)

        When("GET /workspace/{workspace}/webhooks를 호출하면") {
            Then("200 OK가 반환된다") {
                client.get()
                    .uri("/workspace/$workspace/webhooks")
                    .header("Accept", "application/vnd.sayaya.handbook.v1+json")
                    .exchange()
                    .expectStatus().isOk
            }
        }
    }

    Given("웹훅 삭제 API") {
        val webhookId = UUID.randomUUID()
        every { service.delete(webhookId) } returns Mono.empty()

        When("DELETE /workspace/{workspace}/webhooks/{id}를 호출하면") {
            Then("204 No Content가 반환된다") {
                client.delete()
                    .uri("/workspace/$workspace/webhooks/$webhookId")
                    .exchange()
                    .expectStatus().isNoContent
            }
        }
    }
})
