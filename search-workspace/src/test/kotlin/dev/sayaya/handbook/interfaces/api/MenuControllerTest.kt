package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Menu
import dev.sayaya.handbook.domain.SessionStateKind
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.test.StepVerifier
import java.security.Principal

class MenuControllerTest : BehaviorSpec({
    val controller = MenuController()
    val client = WebTestClient.bindToController(controller).build()

    Given("메뉴 조회 API") {
        When("미인증 사용자가 GET /menus 를 호출하면") {
            Then("200 OK + 빈 목록이 반환된다 (워크스페이스 메뉴는 로그인 후에만 의미 있음)") {
                client.get()
                    .uri("/menus")
                    .header("Accept", "application/vnd.sayaya.handbook.v1+json")
                    .exchange()
                    .expectStatus().isOk
                    .expectBodyList(Menu::class.java)
                    .hasSize(0)
            }
        }
        When("인증된 사용자의 Principal 이 주입된 상태로 메서드를 호출하면") {
            val principal = Principal { "alice" }
            Then("workspaces 메뉴 단일 엔트리가 반환된다") {
                StepVerifier.create(controller.menus(principal))
                    .assertNext { it shouldBe MenuController.MENU }
                    .verifyComplete()
            }
        }
        When("정적 MENU 를 검사하면") {
            Then("title 은 'workspaces' 이고 bottom=true 로 하단 고정이다") {
                val menu = MenuController.MENU
                menu.title() shouldBe "workspaces"
                menu.order() shouldBe "S"
                menu.bottom() shouldBe true
                menu.script() shouldBe "js/workspace/workspace.nocache.js"
            }
            Then("tools 목록은 'workspace info', 'groups', 'permissions' 순서로 3개다") {
                val toolTitles = MenuController.MENU.tools().map { it.title() }
                toolTitles shouldContainExactly listOf("workspace info", "groups", "permissions")
            }
        }
        When("allowed_session_states 계약을 검사하면") {
            val menu = MenuController.MENU
            Then("IN_WORKSPACE 세션에서는 isAllowedFor == true") {
                menu.isAllowedFor(SessionStateKind.IN_WORKSPACE) shouldBe true
            }
            Then("AUTHENTICATED 세션에서는 isAllowedFor == true (온보딩 유도)") {
                menu.isAllowedFor(SessionStateKind.AUTHENTICATED) shouldBe true
            }
            Then("ANONYMOUS 세션에서는 isAllowedFor == false") {
                menu.isAllowedFor(SessionStateKind.ANONYMOUS) shouldBe false
            }
        }
    }
})
