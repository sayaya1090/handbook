package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Workspace
import dev.sayaya.handbook.usecase.WorkspaceService
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.security.Principal
import java.util.*

class WorkspaceControllerTest : BehaviorSpec({
    val service = mockk<WorkspaceService>()
    val controller = WorkspaceController(service)
    val client = WebTestClient.bindToController(controller).build()

    val workspace = Workspace(UUID.randomUUID(), "TestWorkspace", "테스트")

    // 워크스페이스 수정 API
    Given("워크스페이스 수정 API") {
        every { service.update(any()) } returns Mono.just(workspace)

        When("PUT /workspace/{id}를 호출하면") {
            Then("200 OK가 반환된다") {
                client.put()
                    .uri("/workspace/${workspace.id}")
                    .header("Content-Type", "application/vnd.sayaya.handbook.v1+json")
                    .bodyValue(WorkspaceController.UpdateWorkspaceRequest("TestWorkspace", "테스트"))
                    .exchange()
                    .expectStatus().isOk
            }
        }
    }

    // 워크스페이스 삭제 API
    Given("워크스페이스 삭제 API") {
        every { service.delete(workspace.id) } returns Mono.empty()

        When("DELETE /workspace/{id}를 호출하면") {
            Then("204 No Content가 반환된다") {
                client.delete()
                    .uri("/workspace/${workspace.id}")
                    .exchange()
                    .expectStatus().isNoContent
            }
        }
    }

    // 워크스페이스 참여(Join) API
    Given("워크스페이스 참여 API") {
        every { service.join(workspace.id, any()) } returns Mono.empty()

        When("POST /workspace/{id}/join을 호출하면") {
            Then("204 No Content가 반환된다") {
                client.post()
                    .uri("/workspace/${workspace.id}/join")
                    .exchange()
                    .expectStatus().isNoContent
            }
        }
    }
})
