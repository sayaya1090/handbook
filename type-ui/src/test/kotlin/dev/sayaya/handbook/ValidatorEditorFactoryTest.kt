package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe

/**
 * ValidatorEditorFactory SOLID 리팩토링 검증 테스트
 */
@GwtHtml("canvastest.html")
internal class ValidatorEditorFactoryTest: GwtTestSpec({
    Given("ValidatorEditorFactory가 초기화됨") {
        page.waitForFunction("window.testValidatorFactory !== undefined")
        val types = listOf("text", "number", "date", "enum", "file", "document", "array", "map")
        
        types.forEach { type ->
            Then("$type 타입에 대한 에디터가 생성되어야 한다") {
                val exists = page.evaluate("window.testValidatorFactory.exists('$type')") as Boolean
                exists shouldBe true
            }
        }

        Then("지원하지 않는 타입(invalid)에 대해서는 null을 반환해야 한다") {
            val exists = page.evaluate("window.testValidatorFactory.exists('invalid')") as Boolean
            exists shouldBe false
        }

        Then("bool 타입은 현재 에디터가 없으므로 null을 반환해야 한다") {
            val exists = page.evaluate("window.testValidatorFactory.exists('bool')") as Boolean
            exists shouldBe false
        }
    }
})
