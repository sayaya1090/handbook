package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import com.microsoft.playwright.options.WaitForSelectorState
import com.microsoft.playwright.Page
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

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
            page.click(".doc-ctrl-btn-add")
            Thread.sleep(1000)
            Then("Undo 버튼이 활성화된다") {
                val disabled = page.querySelector(".doc-ctrl-btn-undo")!!
                    .evaluate("el => el.disabled") as Boolean
                disabled shouldBe false
            }
        }

        When("행을 선택 후 Delete 버튼을 클릭하면") {
            page.evaluate("""
                (function() {
                    var td = document.querySelector('.handsontable td');
                    if (td) td.click();
                })()
            """.trimIndent())
            Thread.sleep(1000)
            page.click(".doc-ctrl-btn-delete")
            Thread.sleep(1000)
            Then("삭제 실행 후 스프레드시트가 유지된다") {
                page.querySelector(".doc-spreadsheet") shouldNotBe null
            }
        }
    }
})
