package dev.sayaya.handbook.interfaces.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.Search
import dev.sayaya.handbook.usecase.DocumentSearchService
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

class ExportControllerTest : BehaviorSpec({
    val service = mockk<DocumentSearchService>()
    val objectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerModule(KotlinModule.Builder().build())
    val controller = ExportController(service, objectMapper)
    val client = WebTestClient.bindToController(controller)
        .argumentResolvers { it.addCustomResolver(SearchArgumentResolver()) }
        .build()
    val workspace = UUID.randomUUID()

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

    Given("JSON 형식 내보내기 API") {
        every { service.findAllForExport(workspace, any()) } returns Mono.just(listOf(doc))

        When("GET /workspace/{id}/documents/export?format=json을 호출하면") {
            Then("200 OK + JSON 응답이 반환된다") {
                client.get()
                    .uri("/workspace/$workspace/documents/export?format=json&page=0&limit=10")
                    .exchange()
                    .expectStatus().isOk
                    .expectHeader().contentType("application/json")
                    .expectHeader().valueEquals("Content-Disposition", "attachment; filename=\"documents.json\"")
                    .expectBody(String::class.java)
                    .consumeWith { result ->
                        val body = result.responseBody!!
                        assert(body.contains("CUST-001"))
                        assert(body.contains("홍길동"))
                    }
            }
        }
    }

    Given("CSV 형식 내보내기 API") {
        every { service.findAllForExport(workspace, any()) } returns Mono.just(listOf(doc))

        When("GET /workspace/{id}/documents/export?format=csv를 호출하면") {
            Then("200 OK + CSV 응답이 반환된다") {
                client.get()
                    .uri("/workspace/$workspace/documents/export?format=csv&page=0&limit=10")
                    .exchange()
                    .expectStatus().isOk
                    .expectHeader().contentType("text/csv")
                    .expectHeader().valueEquals("Content-Disposition", "attachment; filename=\"documents.csv\"")
                    .expectBody(String::class.java)
                    .consumeWith { result ->
                        val body = result.responseBody!!
                        val lines = body.trim().lines()
                        assert(lines.size == 2) { "Expected 2 lines (header + 1 data), got ${lines.size}" }
                        assert(lines[0].contains("type"))
                        assert(lines[1].contains("CUST-001"))
                    }
            }
        }
    }

    Given("지원하지 않는 형식 요청") {
        When("format=xml을 요청하면") {
            Then("400 Bad Request가 반환된다") {
                client.get()
                    .uri("/workspace/$workspace/documents/export?format=xml&page=0&limit=10")
                    .exchange()
                    .expectStatus().isBadRequest
            }
        }
    }

    Given("빈 문서 리스트 내보내기") {
        every { service.findAllForExport(workspace, any()) } returns Mono.just(emptyList())

        When("문서가 없을 때 CSV 내보내기를 호출하면") {
            Then("헤더만 포함된 빈 CSV가 반환된다") {
                client.get()
                    .uri("/workspace/$workspace/documents/export?format=csv&page=0&limit=10")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(String::class.java)
                    .consumeWith { result ->
                        val body = result.responseBody!!
                        val lines = body.trim().lines()
                        assert(lines.size == 1) { "Expected 1 line (header only), got ${lines.size}" }
                        assert(lines[0].contains("type"))
                    }
            }
        }
    }
})
