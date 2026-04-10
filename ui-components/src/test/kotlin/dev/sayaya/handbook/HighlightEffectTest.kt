package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("src/test/webapp/uicomponentstest.html")
internal class HighlightEffectTest: GwtTestSpec({
    Given("UI 컴포넌트가 초기화됨") {
        Then("대상 요소가 존재한다") {
            page.querySelector("#target") shouldNotBe null
        }

        When("Highlight 버튼을 클릭하면") {
            Then("클릭 전에는 ui-highlight 클래스가 없다") {
                val target = page.querySelector("#target")
                val classes = target!!.getAttribute("class") ?: ""
                classes.contains("ui-highlight") shouldBe false
            }

            page.click("#btn-highlight")
            Thread.sleep(500)

            Then("대상 요소에 ui-highlight 클래스가 추가된다") {
                val target = page.querySelector("#target")
                val classes = target!!.getAttribute("class") ?: ""
                classes.contains("ui-highlight") shouldBe true
            }
        }

        When("Clear 버튼을 클릭하면") {
            page.click("#btn-clear")
            Thread.sleep(500)

            Then("ui-highlight 클래스가 제거된다") {
                val target = page.querySelector("#target")
                val classes = target!!.getAttribute("class") ?: ""
                classes.contains("ui-highlight") shouldBe false
            }
        }

        // 재강조 테스트
        When("Highlight를 다시 클릭하면") {
            page.click("#btn-highlight")
            Thread.sleep(500)

            Then("ui-highlight 클래스가 다시 추가된다") {
                val target = page.querySelector("#target")
                val classes = target!!.getAttribute("class") ?: ""
                classes.contains("ui-highlight") shouldBe true
            }
        }

        // 이전 강조가 자동 해제되는지 확인 (같은 대상이므로 계속 유지)
        When("Highlight 후 Clear 없이 다시 Highlight하면") {
            page.click("#btn-highlight")
            Thread.sleep(500)

            Then("대상에 ui-highlight가 유지된다") {
                val target = page.querySelector("#target")
                val classes = target!!.getAttribute("class") ?: ""
                classes.contains("ui-highlight") shouldBe true
            }

            Then("ui-highlight 클래스는 1개만 존재한다") {
                val highlighted = page.querySelectorAll(".ui-highlight")
                highlighted.count() shouldBe 1
            }
        }
    }
})
