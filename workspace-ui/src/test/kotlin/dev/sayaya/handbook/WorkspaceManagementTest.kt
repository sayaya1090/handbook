package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("workspace.html")
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

        When("'Groups & Members' 탭을 클릭하면") {
            val tabs = page.querySelectorAll(".mgmt-tab")
            tabs[1].click()
            // 패널 전환 대기
            page.waitForSelector(".mgmt-dual-panel")
            
            Then("해당 탭이 활성화된다") {
                val activeTab = page.querySelector(".mgmt-tab-active")
                activeTab!!.textContent() shouldBe "Groups & Members"
            }
            Then("그룹 및 멤버 관리 패널(.mgmt-dual-panel)이 렌더링된다") {
                page.isVisible(".mgmt-dual-panel") shouldBe true
            }
        }

        When("'Roles & Permissions' 탭을 클릭하면") {
            val tabs = page.querySelectorAll(".mgmt-tab")
            tabs[2].click()
            // 권한 패널 전환 대기
            page.waitForSelector(".mgmt-role-list")
            
            Then("해당 탭이 활성화된다") {
                val activeTab = page.querySelector(".mgmt-tab-active")
                activeTab!!.textContent() shouldBe "Roles & Permissions"
            }
            Then("권한 관리 패널이 렌더링된다") {
                page.isVisible(".mgmt-role-list") shouldBe true
            }
        }
    }
})
