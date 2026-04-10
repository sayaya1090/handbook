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
        val doc = Document(
            id = UUID.randomUUID(),
            type = "customer",
            serial = "CUST-001",
            effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
            createDateTime = Instant.now(),
            creator = "user-1",
            data = mapOf("name" to "홍길동"),
        )
        every { service.save(workspace, any()) } returns Flux.just(doc)

        When("PUT /workspace/{id}/documents를 호출하면") {
            Then("200 OK가 반환된다") {
                client.put()
                    .uri("/workspace/$workspace/documents")
                    .header("Content-Type", "application/vnd.sayaya.handbook.v1+json")
                    .bodyValue(listOf(doc))
                    .exchange()
                    .expectStatus().isOk
            }
        }
    }

    Given("문서 삭제 API") {
        val doc = Document(
            id = UUID.randomUUID(),
            type = "customer",
            serial = "CUST-002",
            effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
            createDateTime = Instant.now(),
            creator = "user-1",
            data = emptyMap(),
        )
        every { service.delete(workspace, any()) } returns Mono.empty()

        When("DELETE /workspace/{id}/documents를 호출하면") {
            Then("204 No Content가 반환된다") {
                client.delete()
                    .uri("/workspace/$workspace/documents")
                    .header("Content-Type", "application/vnd.sayaya.handbook.v1+json")
                    .exchange()
                    .expectStatus().isNoContent
            }
        }
    }
})
