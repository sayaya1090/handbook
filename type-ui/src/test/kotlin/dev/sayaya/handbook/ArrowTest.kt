package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("canvastest.html")
internal class ArrowTest: GwtTestSpec({
    Given("캔버스가 초기화됨") {
        // Document 참조 화살표 존재
        Then("Document 참조 화살표가 그려진다") {
            val arrows = page.querySelectorAll(".box-reference-container svg")
            arrows.count() shouldBe 1
        }

        // UC-T18: 화살표 호버 하이라이트
        When("화살표를 호버하면") {
            val arrow = page.querySelector(".box-ref-arrow")
            if (arrow != null) {
                val fromKey = arrow.getAttribute("data-from-key")
                val toKey = arrow.getAttribute("data-to-key")
                val attrName = arrow.getAttribute("data-attr-name")
                arrow.hover()
                Thread.sleep(300)
                Then("화살표에 hover 클래스가 추가된다") {
                    val hasHover = arrow.evaluate("el => el.classList.contains('box-ref-hover')") as Boolean
                    hasHover shouldBe true
                }
                Then("화살표 선이 두꺼워진다") {
                    val strokeWidth = arrow.evaluate("el => el.querySelector('.box-ref-line')?.getAttribute('stroke-width')") as String?
                    // CSS에 의해 3으로 변경
                    strokeWidth shouldNotBe null
                }
                Then("참조받는 타입 박스에 하이라이트가 적용된다") {
                    val highlighted = page.querySelector(".type-box.ref-highlight-target")
                    highlighted shouldNotBe null
                }
                Then("참조하는 속성 행에 하이라이트가 적용된다") {
                    val sourceRow = page.querySelector(".type-attr-row.ref-highlight-source")
                    sourceRow shouldNotBe null
                }
                Then("화살표 data 속성에 올바른 정보가 있다") {
                    fromKey shouldNotBe null
                    toKey shouldNotBe null
                    attrName shouldNotBe null
                }
            }
        }
        When("화살표에서 마우스를 떼면") {
            val arrow = page.querySelector(".box-ref-arrow")
            if (arrow != null) {
                // 다른 곳으로 마우스 이동
                page.mouse().move(0.0, 0.0)
                Thread.sleep(300)
                Then("hover 클래스가 제거된다") {
                    val hasHover = arrow.evaluate("el => el.classList.contains('box-ref-hover')") as Boolean
                    hasHover shouldBe false
                }
                Then("타입 박스 하이라이트가 해제된다") {
                    val highlighted = page.querySelector(".type-box.ref-highlight-target")
                    highlighted shouldBe null
                }
                Then("속성 행 하이라이트가 해제된다") {
                    val sourceRow = page.querySelector(".type-attr-row.ref-highlight-source")
                    sourceRow shouldBe null
                }
            }
        }
    }
})
