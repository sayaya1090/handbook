package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("canvastest.html")
internal class UndoRedoTest: GwtTestSpec({
    Given("캔버스가 초기화됨") {
        // UC-T9: Undo/Redo
        When("Ctrl+Z를 누르면") {
            // 먼저 삭제를 수행
            page.click(".type-box[data-type-key='order:1.0']")
            Thread.sleep(500)
            page.evaluate("document.querySelector('.type-canvas').focus()")
            Thread.sleep(200)
            page.keyboard().press("Delete")
            Thread.sleep(1000)
            val before = page.querySelectorAll(".type-box").count()
            page.evaluate("document.querySelector('.type-canvas').focus()")
            page.keyboard().press("Control+z")
            Thread.sleep(500)
            Then("삭제가 되돌려진다") {
                val after = page.querySelectorAll(".type-box").count()
                after shouldBe before + 1
            }
        }
        When("Ctrl+Shift+Z를 누르면") {
            val before = page.querySelectorAll(".type-box").count()
            page.evaluate("document.querySelector('.type-canvas').focus()")
            page.keyboard().press("Control+Shift+z")
            Thread.sleep(500)
            Then("Redo가 실행된다") {
                val after = page.querySelectorAll(".type-box").count()
                after shouldBe before - 1
            }
        }

        // UC-T8: 기간 이동 버튼 존재 확인
        Then("Before/After 버튼이 존재한다") {
            page.querySelector(".type-status-header .type-ctrl-btn-before") shouldNotBe null
            page.querySelector(".type-status-header .type-ctrl-btn-after") shouldNotBe null
        }

        // UC-T10: 저장/로드 버튼 존재 확인
        Then("Save/Reload 버튼이 존재한다") {
            page.querySelector(".type-ctrl-btn-save") shouldNotBe null
            page.querySelector(".type-ctrl-btn-reload") shouldNotBe null
        }

        // UC-T2: 더티 트래킹 — 생성된 타입에 더티 상태 적용
        When("Add Type으로 타입을 추가하면") {
            page.click(".type-ctrl-btn-add")
            Thread.sleep(500)
            Then("Save 버튼이 활성화된다") {
                val disabled = page.querySelector(".type-ctrl-btn-save")!!
                    .evaluate("el => el.disabled") as Boolean
                disabled shouldBe false
            }
        }

        // UC-T9: Undo -> Undo 스택 비움 -> Undo 비활성화
        When("Undo로 타입 생성을 되돌리면") {
            page.evaluate("document.querySelector('.type-canvas').focus()")
            // 모든 액션을 Undo (Undo 버튼이 비활성화될 때까지)
            for (i in 1..10) {
                val undoDisabled = page.querySelector(".type-ctrl-btn-undo")!!
                    .evaluate("el => el.disabled") as Boolean
                if (undoDisabled) break
                page.keyboard().press("Control+z")
                Thread.sleep(500)
            }
            Then("Undo 스택이 비어 Undo 버튼이 비활성화된다") {
                val disabled = page.querySelector(".type-ctrl-btn-undo")!!
                    .evaluate("el => el.disabled") as Boolean
                disabled shouldBe true
            }
        }

        // UC-T21: 에이전트가 타입 생성 + 사용자가 Undo
        When("에이전트가 타입을 생성한 직후 사용자가 Undo를 누르면") {
            val before = page.querySelectorAll(".type-box").count()
            page.evaluate("""
                (function() {
                    var detail = ['CREATE type:agent-undo-test'];
                    var evt = new CustomEvent('handbook-mutate', {detail: detail, bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(500)
            val afterCreate = page.querySelectorAll(".type-box").count()
            Then("에이전트 생성 후 타입이 1개 증가했다") {
                afterCreate shouldBe before + 1
            }
            page.evaluate("document.querySelector('.type-canvas').focus()")
            page.keyboard().press("Control+z")
            Thread.sleep(500)
            Then("Undo 후 타입 개수가 원래대로 돌아온다") {
                val afterUndo = page.querySelectorAll(".type-box").count()
                afterUndo shouldBe before
            }
            Then("Undo 후 Redo 버튼이 활성화된다") {
                val redoDisabled = page.querySelector(".type-ctrl-btn-redo")!!
                    .evaluate("el => el.disabled") as Boolean
                redoDisabled shouldBe false
            }
        }
    }
})
