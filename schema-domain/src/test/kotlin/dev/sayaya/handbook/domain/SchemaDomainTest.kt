package dev.sayaya.handbook.domain

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe

@GwtHtml("schema_domain.html")
class SchemaDomainTest : GwtTestSpec({
    Given("LayoutPeriod 도메인 로직") {
        Then("두 기간의 중첩 영역이 올바르게 계산되어야 한다") {
            page shouldContainLog "LOG_OVERLAP_RESULT:50"
        }
    }
    Given("AttributeTypeValue 도메인 로직") {
        Then("복합 타입 이름이 올바르게 단순화되어야 한다") {
            page shouldContainLog "LOG_SIMPLIFY_RESULT:text[]"
        }
    }
})
