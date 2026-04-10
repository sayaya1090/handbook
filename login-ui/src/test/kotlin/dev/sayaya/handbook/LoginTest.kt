package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldNotBe

@GwtHtml("src/test/webapp/logintest.html")
internal class LoginTest : GwtTestSpec({
    Given("로그인 화면이 로드됨") {
        Thread.sleep(3000)

        Then("콘텐츠 영역이 존재한다") {
            page.querySelector(".login-content") shouldNotBe null
        }

        Then("콘솔 영역이 존재한다") {
            page.querySelector(".console") shouldNotBe null
        }

        When("OAuth 버튼이 렌더링되면") {
            Thread.sleep(500)
            Then("Google 로그인 버튼이 존재한다") {
                page.querySelector(".btn-google") shouldNotBe null
            }
        }
    }
})
