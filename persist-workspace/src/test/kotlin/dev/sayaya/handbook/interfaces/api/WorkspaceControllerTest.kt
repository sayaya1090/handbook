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
        val creatorSlot = slot<UUID>()
        val principalSlot = slot<java.security.Principal>()
        val nameSlot = slot<String>()
        every {
            service.create(capture(creatorSlot), capture(principalSlot), capture(nameSlot), any())
        } returns Mono.just(workspace)

        When("유효한 이름과 UserAuthentication 으로 create 를 호출하면") {
            val request = WorkspaceController.CreateWorkspaceRequest("TestWorkspace", "테스트")
            val result = controller.create(testPrincipal, request).block()

            Then("서비스가 creator UUID / principal / 이름 / 설명으로 호출되고 생성된 워크스페이스가 반환된다") {
                result shouldBe workspace
                // creator UUID = testPrincipal.id (sub 가 null 이면 id 폴백 — 기존 토큰 호환).
                creatorSlot.captured shouldBe UUID.fromString(testUserId)
                // principal 이 UserAuthentication 타입으로 service 에 전달됨 (auth-expert 회귀 방지).
                principalSlot.captured shouldBeSameInstanceAs testPrincipal
                (principalSlot.captured as UserAuthentication).id shouldBe testUserId
                nameSlot.captured shouldBe "TestWorkspace"
                verify(exactly = 1) { service.create(any(), any(), "TestWorkspace", "테스트") }
            }
        }

        When("sub 클레임이 있는 최신 토큰으로 create 를 호출하면") {
            val subUuid = UUID.randomUUID().toString()
            val jtiUuid = UUID.randomUUID().toString() // 다른 값 — 토큰 ID 와 user UUID 가 분리된 Phase 1a 이후 경로
            val principal = UserAuthentication(
                id = jtiUuid,
                username = "Test User 2",
                issuer = "test",
                issuedDateTime = LocalDateTime.now(),
                notBeforeDateTime = LocalDateTime.now(),
                expireDateTime = LocalDateTime.now().plusHours(1),
                token = "dummy.jwt.token",
                sub = subUuid,
            )
            val request = WorkspaceController.CreateWorkspaceRequest("SubToken", null)
            controller.create(principal, request).block()

            Then("sub 가 우선되어 creator UUID 로 service 에 전달된다 (group_member.member 와 일관)") {
                creatorSlot.captured shouldBe UUID.fromString(subUuid)
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

    // 워크스페이스 수정 API — UserAuthentication 주입을 확인하기 위해 직접 메서드 호출.
    Given("워크스페이스 수정 API") {
        val modifierSlot = slot<UUID>()
        every { service.update(any(), capture(modifierSlot)) } returns Mono.just(workspace)

        When("UserAuthentication 과 함께 update 를 호출하면") {
            val request = WorkspaceController.UpdateWorkspaceRequest("TestWorkspace", "테스트")
            val result = controller.update(workspace.id, testPrincipal, request).block()

            Then("수정자 UUID 가 service 에 전달되고 수정된 워크스페이스가 반환된다") {
                result shouldBe workspace
                modifierSlot.captured shouldBe UUID.fromString(testUserId)
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
