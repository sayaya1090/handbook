package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe

/**
 * UC-WM1: 워크스페이스 정보 수정
 */
@GwtHtml("workspace_management_test.html")
internal class WorkspaceManagementTest: GwtTestSpec({
    Given("워크스페이스 관리 UI가 초기화됨") {
        Then("워크스페이스 컨테이너(.workspace-mgmt-container)가 렌더링된다") {
            // GWT 렌더링 완료까지 최대 15초 대기
            page.waitForFunction("() => document.querySelector('.workspace-mgmt-container') !== null", null, 
                com.microsoft.playwright.Page.WaitForFunctionOptions().setTimeout(15000.0))
            page.isVisible(".workspace-mgmt-container") shouldBe true
        }

        Then("정확히 3개의 탭(.mgmt-tab)이 존재한다") {
            val tabs = page.querySelectorAll(".mgmt-tab")
            tabs.size shouldBe 3
        }

        Then("초기에 'General' 탭이 활성화 상태(.mgmt-tab-active)이다") {
            val activeTab = page.querySelector(".mgmt-tab-active")
            activeTab!!.textContent() shouldBe "General"
        }

    }
})
