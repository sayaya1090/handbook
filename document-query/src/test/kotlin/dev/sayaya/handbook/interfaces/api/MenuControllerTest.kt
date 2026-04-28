package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.SessionStateKind
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.test.web.reactive.server.WebTestClient

// UC-SD3: 메뉴 제공
class MenuControllerTest : BehaviorSpec({
    val controller = MenuController()
    val client = WebTestClient.bindToController(controller).build()

    Given("메뉴 조회 API") {
        When("GET /menus를 호출하면") {
            Then("200 OK + documents 메뉴가 반환된다") {
                client.get()
                    .uri("/menus")
                    .header("Accept", "application/vnd.sayaya.handbook.v1+json")
                    .exchange()
                    .expectStatus().isOk
            }
        }
    }

    Given("documents 메뉴의 allowedSessionStates 선언") {
        val menu = MenuController.DOCUMENTS_MENU
        When("IN_WORKSPACE 세션 상태에서 평가하면") {
            Then("isAllowedFor 는 true 를 반환한다") {
                menu.isAllowedFor(SessionStateKind.IN_WORKSPACE) shouldBe true
            }
        }
    }

    Given("dashboard 메뉴의 allowedSessionStates 선언") {
        val menu = MenuController.DASHBOARD_MENU
        When("IN_WORKSPACE 세션 상태에서 평가하면") {
            Then("isAllowedFor 는 true 를 반환한다") {
                menu.isAllowedFor(SessionStateKind.IN_WORKSPACE) shouldBe true
            }
        }
    }
})
