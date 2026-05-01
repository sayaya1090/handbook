package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldNotBe

@GwtHtml("documenttest.html")
internal class DocumentInputTest: GwtTestSpec({
    Given("문서 UI가 초기화됨") {
        page.onConsoleMessage { println("[BROWSER] ${it.type()}: ${it.text()}") }
        Thread.sleep(5000)
        
        Then("스프레드시트 컬럼 헤더가 렌더링된다") {
            page.waitForSelector(".handsontable thead th", com.microsoft.playwright.Page.WaitForSelectorOptions().setTimeout(10000.0)) shouldNotBe null
        }
        
        // ... (나머지 테스트 케이스도 동일하게 처리)
    }
})
