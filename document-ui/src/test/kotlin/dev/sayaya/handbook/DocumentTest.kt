package dev.sayaya.handbook

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.WaitForSelectorState
import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * UC-D1: 문서 조회
 * UC-D2: 문서 생성
 * UC-D3: 문서 편집
 * UC-D4: 문서 삭제
 * UC-D5: 저장
 * UC-D6: 타입 전환
 * UC-D7: Undo/Redo
 * UC-D8: 페이지네이션
 */
@GwtHtml("documenttest.html")
internal class DocumentTest: GwtTestSpec({
    Given("문서 UI가 초기화됨") {
        Thread.sleep(5000)

        Then("컨테이너가 존재한다") {
            page.waitForSelector(".doc-container", Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED)) shouldNotBe null
        }
        Then("컨트롤러 툴바가 존재한다") {
            page.waitForSelector(".doc-controller", Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED)) shouldNotBe null
        }
        Then("타입 탭이 존재한다") {
            page.waitForSelector(".doc-type-tabs", Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED)) shouldNotBe null
        }
        Then("타입 탭 2개가 렌더링된다") {
            val tabs = page.querySelectorAll(".doc-type-tab")
            tabs.count() shouldBe 2
        }
        Then("스프레드시트가 존재한다") {
            page.waitForSelector(".doc-spreadsheet", Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED)) shouldNotBe null
        }

        When("Add 버튼을 클릭하면") {
            page.click(".doc-ctrl-btn-add", Page.ClickOptions().setForce(true))
            Thread.sleep(1000)
            Then("Undo 버튼이 활성화된다") {
                page.waitForSelector(".doc-ctrl-btn-undo:not([disabled])", Page.WaitForSelectorOptions().setTimeout(5000.0))
                val disabled = page.querySelector(".doc-ctrl-btn-undo")!!
                    .evaluate("el => el.disabled") as Boolean
                disabled shouldBe false
            }
        }

        When("행을 선택 후 Delete 버튼을 클릭하면") {
            page.waitForSelector(".handsontable td", Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE))
            page.evaluate("""
                (function() {
                    var td = document.querySelector('.handsontable td');
                    if (td) td.click();
                })()
            """.trimIndent())
            Thread.sleep(500)
            page.click(".doc-ctrl-btn-delete", Page.ClickOptions().setForce(true))
            Thread.sleep(1000)
            Then("삭제 실행 후 스프레드시트가 유지된다") {
                page.querySelector(".doc-spreadsheet") shouldNotBe null
            }
        }
    }
})
