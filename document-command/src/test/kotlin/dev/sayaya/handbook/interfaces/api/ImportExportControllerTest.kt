package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.usecase.DocumentService
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import tools.jackson.databind.ObjectMapper
import java.time.Instant
import java.util.*

/**
 * ImportExportController 단위 테스트.
 *
 * **책임:** UC-PD4(일괄 임포트), UC-PD5(일괄 익스포트) API의 HTTP 레벨 동작을 검증한다.
 *
 * **의존관계:**
 * - [DocumentService] — MockK mock
 * - [ObjectMapper] — Jackson (JavaTimeModule 포함)
 * - [WebTestClient] — 컨트롤러 바인딩 테스트
 */
class ImportExportControllerTest : BehaviorSpec({
    val service = mockk<DocumentService>()
    val objectMapper = ObjectMapper()
    val controller = ImportExportController(service, objectMapper)
    val client = WebTestClient.bindToController(controller).build()
    val workspace = UUID.randomUUID()

    Given("문서 일괄 임포트 API (UC-PD4)") {
        val doc1 = Document.create(
            null,
            "customer",
            "CUST-001",
            Instant.parse("2026-01-01T00:00:00Z").toEpochMilli().toDouble(),
            Instant.parse("2026-12-31T23:59:59Z").toEpochMilli().toDouble(),
            0.0,
            null,
            null
        )
        val doc2 = Document.create(
            null,
            "customer",
            "CUST-002",
            Instant.parse("2026-01-01T00:00:00Z").toEpochMilli().toDouble(),
            Instant.parse("2026-12-31T23:59:59Z").toEpochMilli().toDouble(),
            0.0,
            null,
            null
        )
        val saved1 = Document.create(
            UUID.randomUUID().toString(),
            "customer",
            "CUST-001",
            Instant.parse("2026-01-01T00:00:00Z").toEpochMilli().toDouble(),
            Instant.parse("2026-12-31T23:59:59Z").toEpochMilli().toDouble(),
            Instant.now().toEpochMilli().toDouble(),
            "user-1",
            null
        )
        val saved2 = Document.create(
            UUID.randomUUID().toString(),
            "customer",
            "CUST-002",
            Instant.parse("2026-01-01T00:00:00Z").toEpochMilli().toDouble(),
            Instant.parse("2026-12-31T23:59:59Z").toEpochMilli().toDouble(),
            Instant.now().toEpochMilli().toDouble(),
            "user-1",
            null
        )
        every { service.save(workspace, any()) } returns Flux.just(saved1, saved2)

        When("POST /workspaces/{id}/documents/import를 호출하면") {
            Then("201 Created와 저장된 문서 목록이 반환된다") {
                client.post()
                    .uri("/workspaces/$workspace/documents/import")
                    .header("Content-Type", "application/json")
                    .bodyValue(listOf(doc1, doc2))
                    .exchange()
                    .expectStatus().isCreated
                    .expectBody()
                    .jsonPath("$.length()").isEqualTo(2)
                    .jsonPath("$[0].id").isNotEmpty
                    .jsonPath("$[1].id").isNotEmpty
            }
            Then("DocumentService.save가 호출된다") {
                verify { service.save(workspace, any()) }
            }
        }
    }

    Given("단일 문서 임포트") {
        val doc = Document.create(
            null,
            "order",
            "ORD-001",
            Instant.parse("2026-06-01T00:00:00Z").toEpochMilli().toDouble(),
            Instant.parse("2027-05-31T23:59:59Z").toEpochMilli().toDouble(),
            0.0,
            null,
            null
        )
        val saved = Document.create(
            UUID.randomUUID().toString(),
            "order",
            "ORD-001",
            Instant.parse("2026-06-01T00:00:00Z").toEpochMilli().toDouble(),
            Instant.parse("2027-05-31T23:59:59Z").toEpochMilli().toDouble(),
            Instant.now().toEpochMilli().toDouble(),
            "user-2",
            null
        )
        every { service.save(workspace, any()) } returns Flux.just(saved)

        When("단일 문서를 임포트하면") {
            Then("201 Created가 반환된다") {
                client.post()
                    .uri("/workspaces/$workspace/documents/import")
                    .header("Content-Type", "application/json")
                    .bodyValue(listOf(doc))
                    .exchange()
                    .expectStatus().isCreated
                    .expectBody()
                    .jsonPath("$.length()").isEqualTo(1)
                    .jsonPath("$[0].serial").isEqualTo("ORD-001")
            }
        }
    }

    Given("문서 일괄 익스포트 API (UC-PD5)") {
        val doc1 = Document.create(
            UUID.randomUUID().toString(),
            "customer",
            "CUST-001",
            Instant.parse("2026-01-01T00:00:00Z").toEpochMilli().toDouble(),
            Instant.parse("2026-12-31T23:59:59Z").toEpochMilli().toDouble(),
            Instant.now().toEpochMilli().toDouble(),
            "user-1",
            null
        )
        val doc2 = Document.create(
            UUID.randomUUID().toString(),
            "customer",
            "CUST-002",
            Instant.parse("2026-01-01T00:00:00Z").toEpochMilli().toDouble(),
            Instant.parse("2026-12-31T23:59:59Z").toEpochMilli().toDouble(),
            Instant.now().toEpochMilli().toDouble(),
            "user-1",
            null
        )
        every { service.findAll(workspace, null) } returns Flux.just(doc1, doc2)

        When("GET /workspaces/{id}/documents/export를 호출하면") {
            Then("200 OK와 JSON 첨부 파일이 반환된다") {
                client.get()
                    .uri("/workspaces/$workspace/documents/export")
                    .exchange()
                    .expectStatus().isOk
                    .expectHeader().contentType("application/json")
                    .expectHeader().valueEquals("Content-Disposition", "attachment; filename=\"documents-export.json\"")
                    .expectBody()
                    .jsonPath("$.length()").isEqualTo(2)
                    .jsonPath("$[0].serial").isEqualTo("CUST-001")
                    .jsonPath("$[1].serial").isEqualTo("CUST-002")
            }
        }
    }

    Given("타입 필터링이 적용된 익스포트") {
        val doc = Document.create(
            UUID.randomUUID().toString(),
            "order",
            "ORD-001",
            Instant.parse("2026-06-01T00:00:00Z").toEpochMilli().toDouble(),
            Instant.parse("2027-05-31T23:59:59Z").toEpochMilli().toDouble(),
            Instant.now().toEpochMilli().toDouble(),
            "user-1",
            null
        )
        every { service.findAll(workspace, "order") } returns Flux.just(doc)

        When("type 파라미터로 필터링하면") {
            Then("해당 타입의 문서만 반환된다") {
                client.get()
                    .uri("/workspaces/$workspace/documents/export?type=order")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .jsonPath("$.length()").isEqualTo(1)
                    .jsonPath("$[0].type").isEqualTo("order")
            }
            Then("DocumentService.findAll에 type 파라미터가 전달된다") {
                verify { service.findAll(workspace, "order") }
            }
        }
    }

    Given("문서가 없는 경우의 익스포트") {
        every { service.findAll(workspace, "empty-type") } returns Flux.empty()

        When("문서가 없는 타입으로 익스포트하면") {
            Then("빈 JSON 배열이 반환된다") {
                client.get()
                    .uri("/workspaces/$workspace/documents/export?type=empty-type")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .jsonPath("$.length()").isEqualTo(0)
            }
        }
    }
})
