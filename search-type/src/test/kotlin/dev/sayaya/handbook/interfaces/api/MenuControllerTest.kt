package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.SessionStateKind
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.test.web.reactive.server.WebTestClient

class MenuControllerTest : BehaviorSpec({
    val controller = MenuController()
    val client = WebTestClient.bindToController(controller).build()

    Given("메뉴 조회 API") {
        When("GET /menus를 호출하면") {
            Then("200 OK + types 메뉴가 반환된다") {
                client.get()
                    .uri("/menus")
                    .header("Accept", "application/vnd.sayaya.handbook.v1+json")
                    .exchange()
                    .expectStatus().isOk
            }
        }
    }

    Given("types 메뉴의 세션 상태 제약") {
        val menu = MenuController.MENU
        When("IN_WORKSPACE 세션에서 평가하면") {
            Then("노출 허용") {
                menu.isAllowedFor(SessionStateKind.IN_WORKSPACE) shouldBe true
            }
        }
        When("AUTHENTICATED 세션에서 평가하면") {
            Then("노출 차단 — 워크스페이스 미선택 상태에서는 숨김") {
                menu.isAllowedFor(SessionStateKind.AUTHENTICATED) shouldBe false
            }
        }
        When("ANONYMOUS 세션에서 평가하면") {
            Then("노출 차단 — 익명 사용자에게 숨김") {
                menu.isAllowedFor(SessionStateKind.ANONYMOUS) shouldBe false
            }
        }
        When("allowedSessionStates 집합을 조회하면") {
            Then("IN_WORKSPACE 단일 원소 집합이 반환된다") {
                menu.allowedSessionStatesSet() shouldBe setOf(SessionStateKind.IN_WORKSPACE)
            }
        }
    }
})
