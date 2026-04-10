package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("src/test/webapp/apitest.html")
internal class ApiTest: GwtTestSpec({
    Given("FetchMock 기반 API가 초기화됨") {
        Thread.sleep(2000)

        // UC-S8: 토큰 갱신 및 사용자 정보 조회
        Then("사용자 정보가 로드된다") {
            val status = page.querySelector("#api-test-status")
            status shouldNotBe null
            val text = status!!.textContent()
            (text == "user-loaded" || text == "all-loaded") shouldBe true
        }
        Then("사용자 ID가 표시된다") {
            val userId = page.querySelector("#user-id")
            userId shouldNotBe null
            userId!!.textContent() shouldBe "test-user-id"
        }
        Then("사용자 이름이 표시된다") {
            val userName = page.querySelector("#user-name")
            userName shouldNotBe null
            userName!!.textContent() shouldBe "TestUser"
        }
        Then("메뉴 정보가 로드된다") {
            val menuInfo = page.querySelector("#menu-info")
            menuInfo shouldNotBe null
        }
        Then("메뉴 개수가 표시된다") {
            val menuCount = page.querySelector("#menu-count")
            menuCount shouldNotBe null
            menuCount!!.textContent() shouldBe "1"
        }
        Then("초기화가 완료된다") {
            val status = page.querySelector("#api-test-status")
            status shouldNotBe null
            val text = status!!.textContent()
            (text == "user-loaded" || text == "all-loaded") shouldBe true
        }
    }
})
