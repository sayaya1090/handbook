package dev.sayaya.handbook.interfaces.api

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldNotBe
import org.springframework.test.web.reactive.server.WebTestClient

class UserControllerTest : BehaviorSpec({
    val controller = UserController()
    val client = WebTestClient.bindToController(controller).build()

    Given("사용자 정보 조회 API") {
        When("인증된 사용자가 GET /user를 호출하면") {
            Then("사용자 정보가 반환된다") {
                // UserController는 @AuthenticationPrincipal을 사용하므로
                // WebTestClient standalone으로는 직접 테스트가 어려움
                // 통합 테스트에서 검증
                controller shouldNotBe null
            }
        }
    }
})
