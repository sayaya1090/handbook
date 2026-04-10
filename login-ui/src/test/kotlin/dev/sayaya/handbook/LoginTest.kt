package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeBlank

@GwtHtml("src/test/webapp/logintest.html")
internal class LoginTest : GwtTestSpec({
    Given("로그인 화면이 로드됨") {
        Then("콘텐츠 영역(login-content)이 DOM에 존재한다") {
            page.querySelector(".login-content") shouldNotBe null
        }

        Then("콘솔 영역(console)이 DOM에 존재한다") {
            page.querySelector(".console") shouldNotBe null
        }

        Then("콘솔에 ASCII 아트 웰컴 메시지가 렌더링된다") {
            val console = page.querySelector(".console")
            console shouldNotBe null
            val text = console!!.textContent() ?: ""
            text.shouldContain("v1.0.0")
        }

        Then("콘솔에 라인(.line) 요소가 1개 이상 존재한다") {
            val lines = page.querySelectorAll(".console .line")
            lines.count() shouldBeGreaterThan 0
        }

        When("OAuth 버튼이 렌더링되면") {
            Thread.sleep(500)
            Then("Google 로그인 버튼(btn-google)이 존재한다") {
                page.querySelector(".btn-google") shouldNotBe null
            }
            Then("OAuth 버튼 컨테이너(oauth-buttons)가 존재한다") {
                page.querySelector(".oauth-buttons") shouldNotBe null
            }
            Then("OAuth 버튼에 btn-oauth CSS 클래스가 적용된다") {
                page.querySelector(".btn-oauth") shouldNotBe null
            }
            Then("Google 버튼 텍스트에 Sign in 문구가 포함된다") {
                val btn = page.querySelector(".btn-google")
                btn shouldNotBe null
                val text = btn!!.textContent() ?: ""
                text.shouldContain("Sign in")
                text.shouldContain("Google")
            }
            Then("Google 버튼에 FontAwesome 아이콘(fa-google)이 존재한다") {
                val icon = page.querySelector(".btn-google .fa-google")
                icon shouldNotBe null
            }
            Then("OAuth 버튼이 정확히 1개 존재한다") {
                val buttons = page.querySelectorAll(".oauth-buttons .btn-oauth")
                buttons.count() shouldBe 1
            }
        }
    }
})
