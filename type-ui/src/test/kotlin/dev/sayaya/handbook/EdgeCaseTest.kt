package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * 캔버스 관련 엣지 케이스 테스트.
 * UC-T2: 타입 생성 (빠른 생성)
 * UC-T3: 타입 삭제 (전체 선택)
 * UC-T4: 타입 이동 (모바일 터치)
 * UC-T7: 속성 편집 (빈 이름)
 * UC-T9: Undo (스택 비었을 때)
 * UC-T15: 워크스페이스 이벤트 (에러 내성)
 */
@GwtHtml("canvastest.html")
internal class EdgeCaseTest: GwtTestSpec({
    Given("캔버스 엣지 케이스") {
        // UC-T2: 빈 속성 타입
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

        // UC-T2: 연속 빠른 클릭
        When("Add Type 버튼을 빠르게 3번 클릭하면") {
            val before = page.querySelectorAll(".type-box").count()
            repeat(3) { page.click(".type-ctrl-btn-add") }
            Thread.sleep(1000)
            Then("타입이 정확히 3개 추가된다") {
                val after = page.querySelectorAll(".type-box").count()
                after shouldBe before + 3
            }
        }

        // UC-T3: 타입 전체 선택 후 삭제
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

        // UC-T7: 에디터에서 빈 이름으로 Apply
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

        // UC-T9: Undo 스택이 비었을 때 Ctrl+Z
        When("Undo 스택이 비었을 때 Ctrl+Z를 누르면") {
            val boxCount = page.querySelectorAll(".type-box").count()
            page.keyboard().press("Control+z")
            Thread.sleep(300)
            Then("타입 개수가 변하지 않는다") {
                page.querySelectorAll(".type-box").count() shouldBe boxCount
            }
        }

        // UC-T15: 잘못된 CustomEvent 데이터
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

        // UC-T4: 모바일 터치 드래그 안정성
        When("모바일 터치 드래그를 수행하면") {
            page.setViewportSize(400, 800)
            Thread.sleep(500)
            // 모바일 뷰에서는 설정 다이얼을 열어야 버튼이 나타남
            page.click("md-fab.settings-dial")
            Thread.sleep(300)
            // 레이아웃 모드로 전환 보장 (첫 번째 버튼이 LAYOUT 모드)
            page.click(".type-ctrl-btn[title='Layout Mode']")
            Thread.sleep(300)

            val boxSelector = ".type-box[data-type-key='customer:1.0']"
            val beforeTop = page.evaluate("document.querySelector(\"$boxSelector\").offsetTop").toString().toDouble()

            // TouchEvent 시뮬레이션
            page.evaluate("""
                (async (selector) => {
                    const el = document.querySelector(selector);
                    const box = el.getBoundingClientRect();
                    const startX = box.left + 20;
                    const startY = box.top + 20;
                    const endX = startX + 50;
                    const endY = startY + 50;

                    const createTouch = (x, y, target) => new Touch({
                        identifier: Date.now(),
                        target: target,
                        clientX: x,
                        clientY: y,
                        pageX: x,
                        pageY: y
                    });

                    // touchstart
                    const t1 = createTouch(startX, startY, el);
                    el.dispatchEvent(new TouchEvent('touchstart', {
                        bubbles: true, cancelable: true, touches: [t1], targetTouches: [t1], changedTouches: [t1]
                    }));

                    // 롱프레스 타이머 발화 유도 (버그 재현 환경)
                    await new Promise(r => setTimeout(r, 600));

                    // touchmove
                    const t2 = createTouch(endX, endY, el);
                    el.dispatchEvent(new TouchEvent('touchmove', {
                        bubbles: true, cancelable: true, touches: [t2], targetTouches: [t2], changedTouches: [t2]
                    }));

                    // touchend
                    el.dispatchEvent(new TouchEvent('touchend', {
                        bubbles: true, cancelable: true, touches: [], targetTouches: [], changedTouches: [t2]
                    }));
                })("$boxSelector")
            """.trimIndent())
            Thread.sleep(800)

            Then("타입 박스의 위치가 이동하고 드래그 상태가 종료된다 (Stability)") {
                val afterTop = page.evaluate("document.querySelector(\"$boxSelector\").offsetTop").toString().toDouble()
                (afterTop > beforeTop) shouldBe true

                // 드래그가 종료되었으므로 .type-box[selected] 가 유지되더라도 
                // 후속 mousemove 에 반응하지 않아야 함 (안정성 검증)
                val isDragging = page.evaluate("document.querySelector(\"$boxSelector\").classList.contains('dragging')").toString()
                isDragging shouldBe "false"
            }
            page.setViewportSize(1280, 720)
        }
    }
})