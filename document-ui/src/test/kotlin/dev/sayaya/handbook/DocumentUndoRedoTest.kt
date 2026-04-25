package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("documenttest.html")
internal class DocumentUndoRedoTest: GwtTestSpec({
    Given("문서 UI가 초기화됨") {
        // UC-D7: Undo/Redo
        When("Add 후 Undo 버튼을 클릭하면") {
            page.click(".doc-ctrl-btn-add")
            Thread.sleep(500)
            page.click(".doc-ctrl-btn-undo")
            Thread.sleep(300)
            Then("Undo 후 Redo 버튼이 활성화된다") {
                val disabled = page.querySelector(".doc-ctrl-btn-redo")!!
                    .evaluate("el => el.disabled") as Boolean
                disabled shouldBe false
            }
        }
        When("Redo 버튼을 클릭하면") {
            page.click(".doc-ctrl-btn-redo")
            Thread.sleep(300)
            Then("Redo 후 Undo 버튼이 다시 활성화된다") {
                val disabled = page.querySelector(".doc-ctrl-btn-undo")!!
                    .evaluate("el => el.disabled") as Boolean
                disabled shouldBe false
            }
        }

        // UC-D2: 더티 트래킹 — Add 후 Save 활성화
        When("Add 버튼으로 행을 추가하면") {
            page.click(".doc-ctrl-btn-add")
            Thread.sleep(500)
            Then("스프레드시트에 행이 존재한다") {
                val rows = page.querySelectorAll(".handsontable td")
                rows.count() shouldNotBe 0
            }
            Then("Save 버튼이 활성화된다") {
                val disabled = page.querySelector(".doc-ctrl-btn-save")!!
                    .evaluate("el => el.disabled") as Boolean
                disabled shouldBe false
            }
        }

        // UC-D5: 모든 Undo 후 Undo 버튼 비활성화
        When("Undo로 모든 생성을 되돌리면") {
            // Undo 스택의 모든 액션을 되돌림 (Undo 버튼이 비활성화될 때까지 반복)
            for (i in 1..10) {
                val undoDisabled = page.querySelector(".doc-ctrl-btn-undo")!!
                    .evaluate("el => el.disabled") as Boolean
                if (undoDisabled) break
                page.click(".doc-ctrl-btn-undo")
                Thread.sleep(300)
            }
            Thread.sleep(500)
            Then("Undo 스택이 비어 Undo 버튼이 비활성화된다") {
                val disabled = page.querySelector(".doc-ctrl-btn-undo")!!
                    .evaluate("el => el.disabled") as Boolean
                disabled shouldBe true
            }
        }
    }
})
