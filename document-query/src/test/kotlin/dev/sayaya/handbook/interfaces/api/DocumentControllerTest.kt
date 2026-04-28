package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.Search
import dev.sayaya.handbook.usecase.DocumentSearchService
import io.kotest.core.spec.style.BehaviorSpec
import dev.sayaya.handbook.interfaces.api.SearchArgumentResolver
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

class DocumentControllerTest : BehaviorSpec({
    val service = mockk<DocumentSearchService>()
    val controller = DocumentController(service)
    val client = WebTestClient.bindToController(controller)
        .argumentResolvers { it.addCustomResolver(SearchArgumentResolver()) }
        .build()
    val workspace = UUID.randomUUID()

    Given("문서 검색 API") {
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
        every { service.search(workspace, any()) } returns Mono.just(PageImpl(listOf(doc), PageRequest.of(0, 10), 1))

        When("GET /workspaces/{id}/documents를 호출하면") {
            Then("200 OK + 페이지 결과가 반환된다") {
                client.get()
                    .uri("/workspaces/$workspace/documents?page=0&limit=10")
                    .header("Accept", "application/vnd.sayaya.handbook.v1+json")
                    .exchange()
                    .expectStatus().isOk
            }
        }
    }

    Given("문서 단건 조회 API") {
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
        every { service.find(workspace, "customer", "CUST-001", any()) } returns Mono.just(doc)

        When("GET /workspaces/{id}/customer/CUST-001를 호출하면") {
            Then("200 OK + 문서가 반환된다") {
                client.get()
                    .uri("/workspaces/$workspace/customer/CUST-001?date=2026-06-15T00:00:00Z")
                    .header("Accept", "application/vnd.sayaya.handbook.v1+json")
                    .exchange()
                    .expectStatus().isOk
            }
        }
    }
})
