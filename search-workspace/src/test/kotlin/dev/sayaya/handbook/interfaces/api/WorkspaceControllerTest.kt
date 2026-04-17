package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Workspace
import dev.sayaya.handbook.usecase.WorkspaceSearchService
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

class WorkspaceControllerTest : BehaviorSpec({
    val service = mockk<WorkspaceSearchService>()
    val controller = WorkspaceController(service)
    val client = WebTestClient.bindToController(controller).build()

    Given("두 건의 워크스페이스가 존재하는 상태") {
        val ws1 = Workspace(UUID.randomUUID(), "alpha", "first")
        val ws2 = Workspace(UUID.randomUUID(), "beta", null)
        every { service.list() } returns Flux.just(ws1, ws2)
        every { service.findById(ws1.id) } returns Mono.just(ws1)

        When("GET /workspaces") {
            Then("200 OK + 두 엔트리 반환") {
                client.get().uri("/workspaces")
                    .header("Accept", "application/vnd.sayaya.handbook.v1+json")
                    .exchange()
                    .expectStatus().isOk
                    .expectBodyList(Workspace::class.java)
                    .hasSize(2)
                    .contains(ws1, ws2)
            }
        }

        When("GET /workspaces/{id}") {
            Then("200 OK + 해당 워크스페이스 반환") {
                client.get().uri("/workspaces/${ws1.id}")
                    .header("Accept", "application/vnd.sayaya.handbook.v1+json")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(Workspace::class.java)
                    .isEqualTo(ws1)
            }
        }
    }
})
