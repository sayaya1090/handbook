package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("src/test/webapp/app-test.html")
internal class AppTest: GwtTestSpec({
    Given("앱이 초기화됨") {
        Thread.sleep(3000)

        Then("Shell의 content 요소가 존재한다") {
            page.querySelector("#content") shouldNotBe null
        }

        Then("Shell의 progress bar가 존재한다") {
            page.querySelector(".progress-container") shouldNotBe null
        }

        Then("Agent의 입력창이 존재한다") {
            page.querySelector(".agent-input-container") shouldNotBe null
        }

        Then("Agent의 overlay 컨테이너가 존재한다") {
            page.querySelector(".ui-overlay-container") shouldNotBe null
        }

        Then("Shell과 Agent가 동시에 body에 배치된다") {
            val content = page.querySelector("#content")
            val agentInput = page.querySelector(".agent-input-container")
            content shouldNotBe null
            agentInput shouldNotBe null
        }
    }
})
