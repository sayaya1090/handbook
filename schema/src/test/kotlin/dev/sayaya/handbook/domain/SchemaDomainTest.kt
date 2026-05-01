package dev.sayaya.handbook.domain

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec

@GwtHtml("schemaTest.html")
class SchemaDomainTest : GwtTestSpec({
    beforeTest {
        Thread.sleep(1000)
    }

    Given("Schema 도메인 모델 (공용)") {
        Then("LayoutPeriod 중첩 계산이 정상 작동해야 한다") {
            page shouldContainLog "LOG_OVERLAP_RESULT:50"
        }
        
        Then("AttributeTypeValue 단순화 로직이 정상 작동해야 한다") {
            page shouldContainLog "LOG_SIMPLIFY_RESULT:text[]"
        }

        Then("TypeValue 데이터 할당이 정상 작동해야 한다") {
            page shouldContainLog "LOG_TYPE_RESULT:t-1:1.0:500"
        }

        Then("애플리케이션 준비 완료 로그가 확인되어야 한다") {
            page shouldContainLog "SCHEMA_TEST_READY"
        }
    }
})
