package dev.sayaya.handbook.domain

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe

@GwtHtml("document_shared.html")
class DocumentDomainTest : GwtTestSpec({
    Given("Document 도메인") {
        Then("데이터 할당 및 비즈니스 로직(isExpired)이 정상 작동해야 한다") {
            page shouldContainLog "LOG_DOCUMENT_TEST_START"
            page shouldContainLog "LOG_DOC_DATA:doc-1:type-A:Hello Document"
            page shouldContainLog "LOG_DOC_EXPIRED_TRUE:true"
            page shouldContainLog "LOG_DOC_EXPIRED_FALSE:false"
            page shouldContainLog "DOCUMENT_TEST_READY"
        }
    }
})
