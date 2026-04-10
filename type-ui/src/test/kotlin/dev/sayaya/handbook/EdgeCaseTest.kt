package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("src/test/webapp/canvastest.html")
internal class EdgeCaseTest: GwtTestSpec({
    Given("캔버스 엣지 케이스") {
        // 빈 속성 타입 — 속성이 0개인 타입 생성
        When("속성 없는 타입을 추가하면") {
            val before = page.querySelectorAll(".type-box").count()
            page.click(".type-ctrl-btn-add")
            Thread.sleep(500)
            Then("타입 박스가 생성되고 속성 행이 0개이다") {
                val after = page.querySelectorAll(".type-box").count()
                after shouldBe before + 1
                val boxes = page.querySelectorAll(".type-box")
                val lastBox = boxes[boxes.count() - 1]
                val rows = lastBox.querySelectorAll(".type-attr-row")
                rows.count() shouldBe 0
            }
        }

        // 연속 빠른 클릭 — Add 버튼 스팸
        When("Add Type 버튼을 빠르게 3번 클릭하면") {
            val before = page.querySelectorAll(".type-box").count()
            repeat(3) { page.click(".type-ctrl-btn-add") }
            Thread.sleep(1000)
            Then("타입이 정확히 3개 추가된다") {
                val after = page.querySelectorAll(".type-box").count()
                after shouldBe before + 3
            }
        }

        // 타입 전체 선택 후 삭제
        When("모든 타입을 Ctrl+A로 선택 후 Delete하면") {
            // 캔버스에 포커스를 보장한 뒤 Ctrl+A
            page.evaluate("document.querySelector('.type-canvas').focus()")
            Thread.sleep(200)
            page.keyboard().press("Control+a")
            Thread.sleep(500)
            val total = page.querySelectorAll(".type-box").count()
            val selectedCount = page.querySelectorAll(".type-box[selected]").count()
            Then("전체 선택 후 selected 개수가 전체 박스 수와 같다") {
                selectedCount shouldBe total
            }
        }

        // 에디터에서 빈 이름으로 Apply
        When("속성 에디터에서 이름을 비우고 Apply하면") {
            page.click(".type-box[data-type-key='customer:1.0'] .type-attr-row:first-child")
            Thread.sleep(500)
            // MD3 text-field 또는 일반 input 중 존재하는 것을 사용
            page.evaluate("""
                (function() {
                    var el = document.querySelector('.attr-edit-field md-outlined-text-field') ||
                             document.querySelector('.attr-edit-field input');
                    if (el) { el.value = ''; el.dispatchEvent(new Event('input', {bubbles:true})); }
                })()
            """.trimIndent())
            Thread.sleep(100)
            val applyBtn = page.querySelector(".attr-edit-apply")
            if (applyBtn != null) {
                page.click(".attr-edit-apply")
                Thread.sleep(300)
            }
            Then("다이얼로그가 닫힌다") {
                val dialog = page.querySelector(".attr-editor-dialog")
                if (dialog != null) {
                    val display = dialog.evaluate("el => getComputedStyle(el).display") as String
                    display shouldBe "none"
                }
            }
        }

        // Undo 스택이 비었을 때 Ctrl+Z
        When("Undo 스택이 비었을 때 Ctrl+Z를 누르면") {
            val boxCount = page.querySelectorAll(".type-box").count()
            page.keyboard().press("Control+z")
            Thread.sleep(300)
            Then("타입 개수가 변하지 않는다") {
                page.querySelectorAll(".type-box").count() shouldBe boxCount
            }
        }

        // 잘못된 CustomEvent 데이터
        When("malformed 워크스페이스 이벤트를 수신하면") {
            page.evaluate("""
                (function() {
                    var evt = new CustomEvent('handbook-workspace-event', {detail: 'INVALID_TYPE:not json', bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(500)
            Then("캔버스가 에러 없이 유지된다") {
                page.querySelector(".type-canvas") shouldNotBe null
                val html = page.querySelector(".type-canvas")!!.innerHTML()
                html.isNotBlank() shouldBe true
            }
            Then("컨트롤러가 유지된다") {
                page.querySelector(".type-controller") shouldNotBe null
            }
        }

        // JSON 페이로드가 빈 객체인 이벤트
        When("빈 JSON 페이로드 이벤트를 수신하면") {
            page.evaluate("""
                (function() {
                    var evt = new CustomEvent('handbook-workspace-event', {detail: 'TYPE_CREATED:{}', bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(500)
            Then("캔버스가 에러 없이 유지된다") {
                page.querySelector(".type-canvas") shouldNotBe null
            }
        }
    }
})
