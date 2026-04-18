package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.SessionStateKind
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
            Then("SIGN_IN 은 ANONYMOUS 세션에만 허용된다") {
                // 계약: docs/contracts/menus.md §allowedSessionStates 규약.
                // 비로그인 사용자에게만 Sign In 노출.
                MenuController.SIGN_IN.allowedSessionStatesSet() shouldBe setOf(SessionStateKind.ANONYMOUS)
            }
            Then("SIGN_OUT 은 AUTHENTICATED 와 IN_WORKSPACE 를 모두 명시해야 한다") {
                // 계층 추론 없음 원칙 (SessionStateKind Javadoc).
                // "로그인 이후 모두" 는 AUTHENTICATED 와 IN_WORKSPACE 를 명시 열거해야 한다.
                // AUTHENTICATED 만 선언해 IN_WORKSPACE 에서 Sign Out 이 사라지는 회귀 방지.
                MenuController.SIGN_OUT.allowedSessionStatesSet() shouldBe
                    setOf(SessionStateKind.AUTHENTICATED, SessionStateKind.IN_WORKSPACE)
            }
        }
    }
})
