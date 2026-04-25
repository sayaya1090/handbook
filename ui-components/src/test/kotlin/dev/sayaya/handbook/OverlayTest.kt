package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("uicomponentstest.html")
internal class OverlayTest: GwtTestSpec({
    Given("UI 컴포넌트가 초기화됨") {
        Then("오버레이 컨테이너가 존재한다") {
            page.querySelector(".ui-overlay-container") shouldNotBe null
        }

        Then("오버레이가 초기에는 숨겨져 있다") {
            val overlay = page.querySelector(".ui-overlay-container")
            val display = overlay!!.evaluate("el => getComputedStyle(el).display") as String
            display shouldBe "none"
        }

        When("Overlay coachmark 버튼을 클릭하면") {
            page.click("#btn-overlay")
            Thread.sleep(500)

            Then("오버레이가 표시된다") {
                val overlay = page.querySelector(".ui-overlay-container")
                val display = overlay!!.evaluate("el => getComputedStyle(el).display") as String
                display shouldBe "block"
            }

            Then("코치마크 배경이 렌더링된다") {
                page.querySelector(".ui-coachmark-backdrop") shouldNotBe null
            }

            Then("코치마크 툴팁이 렌더링된다") {
                val tooltip = page.querySelector(".ui-coachmark-tooltip")
                tooltip shouldNotBe null
            }

            Then("툴팁 메시지가 올바르다") {
                val tooltip = page.querySelector(".ui-coachmark-tooltip")
                tooltip!!.textContent() shouldBe "이 영역을 확인하세요"
            }

            Then("위치 클래스가 적용된다") {
                val tooltip = page.querySelector(".ui-coachmark-tooltip")
                val classes = tooltip!!.getAttribute("class") ?: ""
                classes.contains("ui-position-bottom") shouldBe true
            }
        }

        When("오버레이를 클릭하면 (dismissable=true)") {
            page.click(".ui-overlay-container")
            Thread.sleep(500)

            Then("오버레이가 숨겨진다") {
                val overlay = page.querySelector(".ui-overlay-container")
                val display = overlay!!.evaluate("el => getComputedStyle(el).display") as String
                display shouldBe "none"
            }

            Then("코치마크 배경이 제거된다") {
                page.querySelector(".ui-coachmark-backdrop") shouldBe null
            }
        }

        // 오버레이 재표시
        When("오버레이를 닫은 후 다시 클릭하면") {
            page.click("#btn-overlay")
            Thread.sleep(500)

            Then("오버레이가 다시 표시된다") {
                val overlay = page.querySelector(".ui-overlay-container")
                val display = overlay!!.evaluate("el => getComputedStyle(el).display") as String
                display shouldBe "block"
            }

            Then("코치마크 배경이 다시 렌더링된다") {
                page.querySelector(".ui-coachmark-backdrop") shouldNotBe null
            }
        }
    }
})
