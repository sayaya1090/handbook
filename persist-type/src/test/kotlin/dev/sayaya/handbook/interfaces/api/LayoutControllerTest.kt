package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.TypeLayout
import dev.sayaya.handbook.usecase.LayoutService
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

class LayoutControllerTest : BehaviorSpec({
    val service = mockk<LayoutService>()
    val controller = LayoutController(service)
    val client = WebTestClient.bindToController(controller).build()
    val workspace = UUID.randomUUID()

    val layout = TypeLayout(
        id = UUID.randomUUID(),
        workspace = workspace,
        effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
        expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
        positions = mapOf("customer" to TypeLayout.Position(100, 200, 200, 150)),
    )

    // UC-PT4: 레이아웃 조회 API
    Given("레이아웃 조회 API") {
        every { service.findByWorkspace(workspace) } returns Flux.just(layout)

        When("GET /workspace/{id}/layouts를 호출하면") {
            Then("200 OK가 반환된다") {
                client.get()
                    .uri("/workspace/$workspace/layouts")
                    .header("Accept", "application/vnd.sayaya.handbook.v1+json")
                    .exchange()
                    .expectStatus().isOk
            }
        }
    }

    // UC-PT5: 레이아웃 저장 API
    Given("레이아웃 저장 API") {
        every { service.save(workspace, any()) } returns Mono.just(layout)

        When("PUT /workspace/{id}/layouts를 호출하면") {
            Then("200 OK가 반환된다") {
                client.put()
                    .uri("/workspace/$workspace/layouts")
                    .header("Content-Type", "application/vnd.sayaya.handbook.v1+json")
                    .bodyValue(layout)
                    .exchange()
                    .expectStatus().isOk
            }
        }
    }
})
