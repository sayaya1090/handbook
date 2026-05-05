package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Menu
import dev.sayaya.handbook.domain.SessionStateKind
import dev.sayaya.handbook.domain.Workspace
import dev.sayaya.handbook.usecase.WorkspaceReadRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.security.Principal
import java.util.UUID

class MenuControllerTest : BehaviorSpec({
    val repository = mockk<WorkspaceReadRepository>()
    val controller = MenuController(repository)

    Given("MenuController") {
        When("정적 메뉴 상수(MENU, ONBOARDING_MENU)의 속성을 검사하면") {
            Then("MENU의 기본 속성이 올바르다") {
                MenuController.MENU.title() shouldBe "workspaces"
                MenuController.MENU.supportingText() shouldBe "workspace.menu.supporting"
                MenuController.MENU.bottom() shouldBe true
                MenuController.MENU.isAllowedFor(SessionStateKind.IN_WORKSPACE) shouldBe true
                val toolTitles = MenuController.MENU.tools().map { it.title() }
                toolTitles shouldContainExactly listOf("workspace info", "groups", "permissions")
            }
            Then("ONBOARDING_MENU의 기본 속성이 올바르다") {
                MenuController.ONBOARDING_MENU.title() shouldBe "workspace.onboarding"
                MenuController.ONBOARDING_MENU.supportingText() shouldBe "workspace.onboarding.supporting"
                MenuController.ONBOARDING_MENU.url() shouldBe "/workspaces/onboarding"
                MenuController.ONBOARDING_MENU.isAllowedFor(SessionStateKind.AUTHENTICATED) shouldBe true
            }
        }

        When("인증되지 않은 사용자가 GET /menus 를 호출하면") {
            val client = WebTestClient.bindToController(controller).build()
            Then("빈 목록을 반환한다") {
                client.get().uri("/menus")
                    .header("Accept", "application/vnd.sayaya.handbook.v1+json")
                    .exchange()
                    .expectStatus().isOk
                    .expectBodyList(Menu::class.java).hasSize(0)
            }
        }

        When("인증된 사용자의 워크스페이스가 존재하면") {
            val principal = dev.sayaya.handbook.interfaces.authentication.UserAuthentication(
                sub = UUID.randomUUID().toString(),
                id = "test-jti",
                username = "testuser",
                issuer = "test-issuer",
                issuedDateTime = java.time.LocalDateTime.now(),
                notBeforeDateTime = java.time.LocalDateTime.now(),
                expireDateTime = java.time.LocalDateTime.now().plusHours(1),
                token = "test-token"
            )
            every { repository.findByUserSub(any()) } returns Flux.just(mockk<Workspace>())
            Then("workspaces 메뉴를 반환한다") {
                StepVerifier.create(controller.menus(principal))
                    .assertNext { it shouldBe MenuController.MENU }
                    .verifyComplete()
            }
        }

        When("인증된 사용자의 워크스페이스가 없으면") {
            val principal = dev.sayaya.handbook.interfaces.authentication.UserAuthentication(
                sub = UUID.randomUUID().toString(),
                id = "test-jti",
                username = "testuser",
                issuer = "test-issuer",
                issuedDateTime = java.time.LocalDateTime.now(),
                notBeforeDateTime = java.time.LocalDateTime.now(),
                expireDateTime = java.time.LocalDateTime.now().plusHours(1),
                token = "test-token"
            )
            every { repository.findByUserSub(any()) } returns Flux.empty()
            Then("onboarding 메뉴를 반환한다") {
                StepVerifier.create(controller.menus(principal))
                    .assertNext { it shouldBe MenuController.ONBOARDING_MENU }
                    .verifyComplete()
            }
        }
    }
})
