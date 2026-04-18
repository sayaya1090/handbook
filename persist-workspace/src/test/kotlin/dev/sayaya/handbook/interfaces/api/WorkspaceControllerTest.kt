package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Workspace
import dev.sayaya.handbook.interfaces.authentication.UserAuthentication
import dev.sayaya.handbook.usecase.WorkspaceService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

class WorkspaceControllerTest : BehaviorSpec({
    val service = mockk<WorkspaceService>()
    val controller = WorkspaceController(service)

    // HTTP 엔드포인트 검증(PathVariable, NoContent 매핑 등) 용도.
    // create 처럼 @AuthenticationPrincipal 을 받는 경로는 standalone WebTestClient 에서
    // 리졸버 체인이 spring-security 없이는 정상 매핑되지 않으므로 직접 메서드 호출로 검증한다.
    val client = WebTestClient.bindToController(controller).build()

    val testUserId = UUID.randomUUID().toString()
    val testPrincipal = UserAuthentication(
        id = testUserId,
        username = "Test User",
        issuer = "test",
        issuedDateTime = LocalDateTime.now(),
        notBeforeDateTime = LocalDateTime.now(),
        expireDateTime = LocalDateTime.now().plusHours(1),
        token = "dummy.jwt.token",
    )

    val workspace = Workspace(UUID.randomUUID(), "TestWorkspace", "테스트")

    // 워크스페이스 생성 — @AuthenticationPrincipal UserAuthentication 주입 + 이름 정규식 검증.
    Given("워크스페이스 생성 API") {
        val principalSlot = slot<java.security.Principal>()
        val nameSlot = slot<String>()
        every {
            service.create(capture(principalSlot), capture(nameSlot), any())
        } returns Mono.just(workspace)

        When("유효한 이름과 UserAuthentication 으로 create 를 호출하면") {
            val request = WorkspaceController.CreateWorkspaceRequest("TestWorkspace", "테스트")
            val result = controller.create(testPrincipal, request).block()

            Then("서비스가 같은 principal/이름/설명으로 호출되고 생성된 워크스페이스가 반환된다") {
                result shouldBe workspace
                // principal 이 UserAuthentication 타입으로 service 에 전달됨 (auth-expert 회귀 방지).
                principalSlot.captured shouldBeSameInstanceAs testPrincipal
                (principalSlot.captured as UserAuthentication).id shouldBe testUserId
                nameSlot.captured shouldBe "TestWorkspace"
                verify(exactly = 1) { service.create(any(), "TestWorkspace", "테스트") }
            }
        }

        When("정규식에 맞지 않는 이름으로 create 를 호출하면") {
            val request = WorkspaceController.CreateWorkspaceRequest("bad name!", null)

            Then("400 Bad Request 상태의 ResponseStatusException 이 던져진다") {
                val ex = shouldThrow<ResponseStatusException> {
                    controller.create(testPrincipal, request).block()
                }
                ex.statusCode shouldBe HttpStatus.BAD_REQUEST
            }
        }
    }

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
