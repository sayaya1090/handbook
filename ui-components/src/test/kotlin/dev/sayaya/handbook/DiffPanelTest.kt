package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("src/test/webapp/uicomponentstest.html")
internal class DiffPanelTest: GwtTestSpec({
    Given("UI 컴포넌트가 초기화됨") {
        Then("Diff 패널이 초기에는 숨겨져 있다") {
            val panel = page.querySelector(".ui-diff-panel")
            panel shouldNotBe null
            val parent = panel!!.evaluate("el => getComputedStyle(el.parentElement).display") as String
            parent shouldBe "none"
        }

        When("Diff 버튼을 클릭하면") {
            page.click("#btn-diff")
            Thread.sleep(500)

            Then("Diff 패널이 표시된다") {
                val panel = page.querySelector(".ui-diff-panel")
                val parent = panel!!.evaluate("el => getComputedStyle(el.parentElement).display") as String
                parent shouldNotBe "none"
            }

            Then("헤더가 존재한다") {
                val header = page.querySelector(".ui-diff-header")
                header shouldNotBe null
            }

            Then("변경 라인 2개가 표시된다") {
                val lines = page.querySelectorAll(".ui-diff-line")
                lines.count() shouldBe 2
            }

            Then("첫 번째 라인에 before 텍스트가 표시된다") {
                val before = page.querySelector(".ui-diff-before")
                before shouldNotBe null
                before!!.textContent()!!.trim() shouldBe "이름"
            }

            Then("첫 번째 라인에 after 텍스트가 표시된다") {
                val after = page.querySelector(".ui-diff-after")
                after shouldNotBe null
                after!!.textContent()!!.trim() shouldBe "고객명"
            }

            Then("화살표 아이콘이 존재한다") {
                val arrow = page.querySelector(".ui-diff-arrow")
                arrow shouldNotBe null
            }
        }

        When("Hide diff 버튼을 클릭하면") {
            page.click("#btn-hide-diff")
            Thread.sleep(500)

            Then("Diff 패널이 숨겨진다") {
                val panel = page.querySelector(".ui-diff-panel")
                val parent = panel!!.evaluate("el => getComputedStyle(el.parentElement).display") as String
                parent shouldBe "none"
            }

            Then("Diff 콘텐츠가 비워진다") {
                val lines = page.querySelectorAll(".ui-diff-line")
                lines.count() shouldBe 0
            }
        }

        // 재표시 테스트
        When("Diff를 숨긴 후 다시 표시하면") {
            page.click("#btn-diff")
            Thread.sleep(500)

            Then("Diff 패널이 다시 표시된다") {
                val panel = page.querySelector(".ui-diff-panel")
                val parent = panel!!.evaluate("el => getComputedStyle(el.parentElement).display") as String
                parent shouldNotBe "none"
            }

            Then("변경 라인이 다시 렌더링된다") {
                val lines = page.querySelectorAll(".ui-diff-line")
                lines.count() shouldBe 2
            }
        }
    }
})
