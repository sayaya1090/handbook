package dev.sayaya.handbook.interfaces.api

import io.kotest.core.spec.style.BehaviorSpec
import org.springframework.test.web.reactive.server.WebTestClient

class MenuControllerTest : BehaviorSpec({
    val controller = MenuController()
    val client = WebTestClient.bindToController(controller).build()

    Given("메뉴 조회 API") {
        When("GET /menus를 호출하면") {
            Then("200 OK + workspaces 메뉴가 반환된다") {
                client.get()
                    .uri("/menus")
                    .header("Accept", "application/vnd.sayaya.handbook.v1+json")
                    .exchange()
                    .expectStatus().isOk
            }
        }
    }
})
