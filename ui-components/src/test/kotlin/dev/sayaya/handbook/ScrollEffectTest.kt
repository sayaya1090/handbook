package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("uicomponentstest.html")
internal class ScrollEffectTest: GwtTestSpec({
    Given("UI 컴포넌트가 초기화됨") {
        Then("스크롤 대상 요소가 존재한다") {
            page.querySelector("#scroll-target") shouldNotBe null
        }

        When("Scroll 버튼을 클릭하면") {
            Then("클릭 전에는 ui-scroll-arrived 클래스가 없다") {
                val target = page.querySelector("#scroll-target")
                val classes = target!!.getAttribute("class") ?: ""
                classes.contains("ui-scroll-arrived") shouldBe false
            }

            page.click("#btn-scroll")
            Thread.sleep(500)

            Then("대상 요소에 ui-scroll-arrived 클래스가 추가된다") {
                val target = page.querySelector("#scroll-target")
                val classes = target!!.getAttribute("class") ?: ""
                classes.contains("ui-scroll-arrived") shouldBe true
            }

            Then("2초 후 ui-scroll-arrived 클래스가 자동 제거된다") {
                Thread.sleep(2000)
                val target = page.querySelector("#scroll-target")
                val classes = target!!.getAttribute("class") ?: ""
                classes.contains("ui-scroll-arrived") shouldBe false
            }
        }
    }
})
