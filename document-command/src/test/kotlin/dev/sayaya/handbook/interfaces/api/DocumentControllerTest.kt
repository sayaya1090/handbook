package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.usecase.DocumentService
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

class DocumentControllerTest : BehaviorSpec({
    val service = mockk<DocumentService>()
    val controller = DocumentController(service)
    val client = WebTestClient.bindToController(controller).build()
    val workspace = UUID.randomUUID()

    Given("문서 저장 API") {
        val doc = Document.create(
            UUID.randomUUID().toString(),
            "customer",
            "CUST-001",
            Instant.parse("2026-01-01T00:00:00Z").toEpochMilli().toDouble(),
            Instant.parse("2026-12-31T23:59:59Z").toEpochMilli().toDouble(),
            Instant.now().toEpochMilli().toDouble(),
            "user-1",
            null
        )
        every { service.save(workspace, any()) } returns Flux.just(doc)

        When("PUT /workspaces/{id}/documents를 호출하면") {
            Then("200 OK가 반환된다") {
                client.put()
                    .uri("/workspaces/$workspace/documents")
                    .header("Content-Type", "application/vnd.sayaya.handbook.v1+json")
                    .bodyValue(listOf(doc))
                    .exchange()
                    .expectStatus().isOk
            }
        }
    }

    Given("문서 삭제 API") {
        val doc = Document.create(
            UUID.randomUUID().toString(),
            "customer",
            "CUST-002",
            Instant.parse("2026-01-01T00:00:00Z").toEpochMilli().toDouble(),
            Instant.parse("2026-12-31T23:59:59Z").toEpochMilli().toDouble(),
            Instant.now().toEpochMilli().toDouble(),
            "user-1",
            null
        )
        every { service.delete(workspace, any()) } returns Mono.empty()

        When("DELETE /workspaces/{id}/documents를 호출하면") {
            Then("204 No Content가 반환된다") {
                client.method(org.springframework.http.HttpMethod.DELETE)
                    .uri("/workspaces/$workspace/documents")
                    .header("Content-Type", "application/vnd.sayaya.handbook.v1+json")
                    .bodyValue(listOf(doc))
                    .exchange()
                    .expectStatus().isNoContent
            }
        }
    }
})
