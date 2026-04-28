package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("documenttest.html")
internal class DocumentEdgeCaseTest: GwtTestSpec({
    Given("문서 UI 엣지 케이스") {
        page.onConsoleMessage { println("[BROWSER] ${it.type()}: ${it.text()}") }
        Thread.sleep(5000) // 렌더링 대기
        
        When("첫 페이지에서 Prev 버튼을 클릭하면") {
            val prevBtn = page.waitForSelector(".doc-page-prev", com.microsoft.playwright.Page.WaitForSelectorOptions().setTimeout(10000.0))
            prevBtn.click()
            Thread.sleep(500)
            Then("에러 없이 동작한다") {
                // 특정 에러가 없는지 확인
                page.querySelector(".doc-spreadsheet") shouldNotBe null
            }
        }

        When("행을 선택하지 않고 Delete 버튼을 클릭하면") {
            val delBtn = page.waitForSelector(".doc-ctrl-btn-delete", com.microsoft.playwright.Page.WaitForSelectorOptions().setTimeout(10000.0))
            delBtn.click()
            Thread.sleep(500)
            Then("에러 없이 무시된다") {
                page.querySelector(".doc-spreadsheet") shouldNotBe null
            }
        }
    }
})
