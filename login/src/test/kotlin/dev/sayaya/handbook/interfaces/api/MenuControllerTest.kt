package dev.sayaya.handbook.interfaces.api

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.test.web.reactive.server.WebTestClient

class MenuControllerTest : BehaviorSpec({
    val controller = MenuController()
    val client = WebTestClient.bindToController(controller).build()

    Given("메뉴 조회 API") {
        When("미인증 사용자가 GET /menus를 호출하면") {
            Then("SIGN_IN 메뉴가 반환된다") {
                client.get()
                    .uri("/menus")
                    .header("Accept", "application/vnd.sayaya.handbook.v1+json")
                    .exchange()
                    .expectStatus().isOk
            }
        }
        When("정적 Menu 상수의 title 을 검사하면") {
            Then("SIGN_IN 은 i18n 키 login.sign_in 을 가진다") {
                // 표시 리터럴이 아닌 i18n 키. 띄어쓰기·대문자 없는 snake_case + 모듈 namespace.
                // 리터럴 회귀(\"sign in\", \"Sign In\" 등) 방지.
                MenuController.SIGN_IN.title() shouldBe "login.sign_in"
            }
            Then("SIGN_OUT 은 i18n 키 login.sign_out 을 가진다") {
                MenuController.SIGN_OUT.title() shouldBe "login.sign_out"
            }
            Then("두 상수 모두 appBarSlot=trailing, order=Z 로 승격 대상이다") {
                MenuController.SIGN_IN.appBarSlot() shouldBe "trailing"
                MenuController.SIGN_IN.order() shouldBe "Z"
                MenuController.SIGN_OUT.appBarSlot() shouldBe "trailing"
                MenuController.SIGN_OUT.order() shouldBe "Z"
            }
        }
    }
})
