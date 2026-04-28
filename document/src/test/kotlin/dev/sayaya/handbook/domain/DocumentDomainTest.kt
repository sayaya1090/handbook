package dev.sayaya.handbook.domain

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe

@GwtHtml("document_shared.html")
class DocumentDomainTest : GwtTestSpec({
    beforeTest {
        Thread.sleep(1500)
    }

    Given("Environment Check") {
        Then("브라우저 로그가 캡처되어야 한다") {
            page shouldContainLog "LOG_DOCUMENT_TEST_START"
        }
    }
})
