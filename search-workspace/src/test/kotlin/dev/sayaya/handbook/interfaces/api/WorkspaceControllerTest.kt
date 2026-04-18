package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Workspace
import dev.sayaya.handbook.interfaces.authentication.UserAuthentication
import dev.sayaya.handbook.usecase.WorkspaceSearchService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.UUID

class WorkspaceControllerTest : BehaviorSpec({
    val service = mockk<WorkspaceSearchService>()
    val controller = WorkspaceController(service)

    // findById 등 principal 이 필요 없는 엔드포인트는 WebTestClient 검증 유지.
    // list 는 @AuthenticationPrincipal 주입이 필요하므로 메서드 직접 호출로 검증한다
    // (persist-workspace WorkspaceControllerTest 와 동일한 패턴).
    val client = WebTestClient.bindToController(controller).build()

    fun principalWith(sub: String? = null, id: String? = null) = UserAuthentication(
        id = id,
        username = "Test User",
        issuer = "test",
        issuedDateTime = LocalDateTime.now(),
        notBeforeDateTime = LocalDateTime.now(),
        expireDateTime = LocalDateTime.now().plusHours(1),
        token = "dummy.jwt.token",
        sub = sub,
    )

    Given("두 건의 워크스페이스가 해당 사용자에게 보이는 상태") {
        val ws1 = Workspace(UUID.randomUUID(), "alpha", "first")
        val ws2 = Workspace(UUID.randomUUID(), "beta", null)
        val userSub = UUID.randomUUID()
        val principal = principalWith(sub = userSub.toString())
        every { service.listForUser(userSub) } returns Flux.just(ws1, ws2)
        every { service.findById(ws1.id) } returns Mono.just(ws1)

        When("controller.list(principal) 를 호출하면") {
            val result = controller.list(principal).collectList().block()!!

            Then("principal.sub 기반으로 service.listForUser 가 호출되고 두 엔트리가 반환된다") {
                result shouldContainExactly listOf(ws1, ws2)
                verify(exactly = 1) { service.listForUser(userSub) }
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

    Given("principal 에 sub 가 없고 id 만 있는 Phase 1a 이전 토큰") {
        val legacyUuid = UUID.randomUUID()
        val principal = principalWith(sub = null, id = legacyUuid.toString())
        every { service.listForUser(legacyUuid) } returns Flux.empty()

        When("controller.list(principal) 를 호출하면") {
            val result = controller.list(principal).collectList().block()!!

            Then("id(jti) 를 폴백으로 서비스에 전달한다") {
                result shouldBe emptyList()
                verify(exactly = 1) { service.listForUser(legacyUuid) }
            }
        }
    }

    Given("principal 의 sub/id 모두 null") {
        // 개별 service 인스턴스로 호출 0회 검증 — 다른 Given 의 누적을 차단한다.
        val isolated = mockk<WorkspaceSearchService>()
        val isolatedController = WorkspaceController(isolated)
        val principal = principalWith(sub = null, id = null)

        When("controller.list(principal) 를 호출하면") {
            val result = isolatedController.list(principal).collectList().block()!!

            Then("서비스 호출 없이 빈 Flux 를 반환한다") {
                result shouldBe emptyList()
                verify(exactly = 0) { isolated.listForUser(any()) }
            }
        }
    }

    Given("principal 의 sub 가 UUID 로 파싱 불가한 문자열") {
        val isolated = mockk<WorkspaceSearchService>()
        val isolatedController = WorkspaceController(isolated)
        val principal = principalWith(sub = "not-a-uuid")

        When("controller.list(principal) 를 호출하면") {
            val result = isolatedController.list(principal).collectList().block()!!

            Then("파싱 실패 폴백으로 빈 Flux 를 반환한다") {
                result shouldBe emptyList()
                verify(exactly = 0) { isolated.listForUser(any()) }
            }
        }
    }
})
