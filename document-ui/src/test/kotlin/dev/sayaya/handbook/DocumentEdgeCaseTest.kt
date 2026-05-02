package dev.sayaya.handbook

import com.microsoft.playwright.Page
import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldNotBe

@GwtHtml("documenttest.html")
internal class DocumentEdgeCaseTest: GwtTestSpec({
    Given("문서 UI 엣지 케이스") {
        When("첫 페이지에서 Prev 버튼을 클릭하면") {
            // force: true 옵션을 주면 'enabled' 대기를 건너뜁니다.
            page.click(".doc-page-prev", Page.ClickOptions().setForce(true))
            Thread.sleep(500)
            Then("에러 없이 동작한다") {
                page.querySelector(".doc-spreadsheet") shouldNotBe null
            }
        }

        When("행을 선택하지 않고 Delete 버튼을 클릭하면") {
            page.click(".doc-ctrl-btn-delete")
            Thread.sleep(500)
            Then("에러 없이 무시된다") {
                page.querySelector(".doc-spreadsheet") shouldNotBe null
            }
        }
    }
})
