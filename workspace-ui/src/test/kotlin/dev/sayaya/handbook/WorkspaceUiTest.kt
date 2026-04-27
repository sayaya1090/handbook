package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("workspace.html")
internal class WorkspaceUiTest: GwtTestSpec({
    Given("워크스페이스 관리 UI가 초기화됨") {
        page.onConsoleMessage { println("[BROWSER] ${it.type()}: ${it.text()}") }
        page.onPageError { println("[BROWSER ERROR] $it") }

        Then("HTML이 로드되었다") {
            page.waitForSelector("#test-ready") shouldNotBe null
        }

        // 로그를 보기 위해 의도적으로 긴 대기 시간 설정 (GWT 로딩 및 초기화 확인용)
        When("GWT가 초기화될 때까지 대기하면") {
            Thread.sleep(10000)
            Then("탭 컨테이너(.mgmt-tabs)가 DOM에 나타나야 한다") {
                page.waitForSelector(".mgmt-tabs", com.microsoft.playwright.Page.WaitForSelectorOptions().setTimeout(20000.0)) shouldNotBe null
            }
        }

        Then("정확히 3개의 탭(.mgmt-tab)이 존재한다") {
            val tabs = page.querySelectorAll(".mgmt-tab")
            tabs.count() shouldBe 3
        }
        Then("초기에 'General' 탭이 활성화 상태(.mgmt-tab-active)이다") {
            val activeTab = page.querySelector(".mgmt-tab-active")
            activeTab!!.textContent() shouldBe "General"
        }
    }
})
