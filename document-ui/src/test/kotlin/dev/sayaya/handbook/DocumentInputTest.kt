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
            // Target the specific visible header clone to avoid timeouts on hidden clones
            page.waitForSelector(".ht_clone_top thead th", com.microsoft.playwright.Page.WaitForSelectorOptions().setTimeout(10000.0)) shouldNotBe null
        }
    }
})
