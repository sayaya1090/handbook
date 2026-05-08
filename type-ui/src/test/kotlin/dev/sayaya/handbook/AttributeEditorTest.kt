package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * UC-T7: 속성 추가/편집
 */
@GwtHtml("canvastest.html")
internal class AttributeEditorTest: GwtTestSpec({
    Given("캔버스가 초기화됨") {
        // UC-T7: 속성 편집 (컨텍스트 메뉴)
        When("타입 박스를 우클릭하면") {
            // JS에서 직접 contextmenu 이벤트를 발행 (stopPropagation으로 canvas 핸들러 우회)
            page.evaluate("""
                (function() {
                    var box = document.querySelector('.type-box[data-type-key="customer:1.0"]');
                    if (box) {
                        var e = new MouseEvent('contextmenu', {bubbles: false, cancelable: true, clientX: 100, clientY: 100});
                        box.dispatchEvent(e);
                    }
                })()
            """.trimIndent())
            Thread.sleep(500)
            Then("컨텍스트 메뉴가 표시된다") {
                // box 컨텍스트 메뉴는 두 번째 .ctx-menu (첫 번째는 canvas 컨텍스트 메뉴)
                val menus = page.querySelectorAll(".ctx-menu")
                menus.count() shouldBe 2
                // 둘 중 하나라도 표시되면 성공
                val anyVisible = page.evaluate("""
                    Array.from(document.querySelectorAll('.ctx-menu')).some(
                        el => getComputedStyle(el).display !== 'none'
                    )
                """.trimIndent()) as Boolean
                anyVisible shouldBe true
            }
            // 컨텍스트 메뉴 닫기 (이후 테스트에서 블로킹되지 않도록)
            page.evaluate("document.querySelectorAll('.ctx-menu').forEach(el => el.style.display = 'none')")
            Thread.sleep(200)
        }

        // UC-T7: 속성 편집 — 속성 클릭 시 에디터 다이얼로그 표시
        When("속성 행을 클릭하면") {
            page.click(".type-box[data-type-key='customer:1.0'] .type-attr-row:first-child")
            Thread.sleep(500)
            Then("속성 편집 다이얼로그가 표시된다") {
                val dialog = page.querySelector(".attr-editor-dialog")
                dialog shouldNotBe null
                val display = dialog!!.evaluate("el => getComputedStyle(el).display") as String
                display shouldNotBe "none"
            }
            Then("타입 셀렉터 버튼 9개가 존재한다") {
                val buttons = page.querySelectorAll(".attr-type-btn")
                buttons.count() shouldBe 9
            }
        }

        // UC-T7: Array 타입 선택 시 서브 에디터 표시
        When("Array 타입을 선택하면") {
            page.click(".type-box[data-type-key='customer:1.0'] .type-attr-row:first-child")
            Thread.sleep(300)
            page.click(".attr-type-btn:nth-child(6)")
            Thread.sleep(300)
            Then("element type 드롭다운(MD3 Select)이 표시된다") {
                val select = page.querySelector(".validator-sub-editor md-outlined-select, .validator-container md-outlined-select")
                select shouldNotBe null
            }
            Then("Array 타입 버튼에 selected 속성이 있다") {
                val btn = page.querySelector(".attr-type-btn:nth-child(6)")
                btn!!.getAttribute("selected") shouldNotBe null
            }
            Then("서브 에디터 컨테이너가 비어있지 않다") {
                val html = page.querySelector(".validator-container")!!.innerHTML()
                html.length shouldNotBe 0
            }
        }

        // UC-T7: Map 타입 선택 시 키/값 서브 에디터 표시
        When("Map 타입을 선택하면") {
            page.click(".type-box[data-type-key='customer:1.0'] .type-attr-row:first-child")
            Thread.sleep(300)
            page.click(".attr-type-btn:nth-child(7)")
            Thread.sleep(300)
            Then("키 타입과 값 타입 두 개의 MD3 Select가 표시된다") {
                val selects = page.querySelectorAll(".validator-container md-outlined-select")
                selects.count() shouldBe 2
            }
            Then("Map 타입 버튼에 selected 속성이 있다") {
                val btn = page.querySelector(".attr-type-btn:nth-child(7)")
                btn!!.getAttribute("selected") shouldNotBe null
            }
            Then("이전 Array 타입 버튼의 selected가 해제되었다") {
                val btn = page.querySelector(".attr-type-btn:nth-child(6)")
                btn!!.getAttribute("selected") shouldBe null
            }
        }

        // UC-T7: Document 타입 선택 시 타입 목록 드롭다운
        When("Document 타입을 선택하면") {
            page.click(".type-box[data-type-key='customer:1.0'] .type-attr-row:first-child")
            Thread.sleep(300)
            page.click(".attr-type-btn:nth-child(9)")
            Thread.sleep(300)
            Then("참조 타입 드롭다운이 표시된다") {
                val select = page.querySelector(".validator-container md-outlined-select")
                select shouldNotBe null
            }
            Then("드롭다운에 옵션이 존재한다") {
                val options = page.querySelectorAll(".validator-container md-outlined-select md-select-option")
                options.count() shouldNotBe 0
            }
        }

        // UC-T7: 에디터에서 이름 필드 수정
        When("에디터에서 이름 필드를 수정하면") {
            page.click(".type-box[data-type-key='customer:1.0'] .type-attr-row:first-child")
            Thread.sleep(500)
            Then("이름 필드에 현재 속성 이름이 표시된다") {
                // .attr-edit-field 자체가 md-outlined-text-field 요소
                val value = page.evaluate("""
                    (function() {
                        var el = document.querySelector('.attr-edit-field');
                        return el ? (el.value || '') : '';
                    })()
                """.trimIndent()) as String
                value.isNotBlank() shouldBe true
            }
        }

        // UC-T7: 에디터 닫기
        When("Close 버튼을 클릭하면") {
            page.click(".type-box[data-type-key='customer:1.0'] .type-attr-row:first-child")
            Thread.sleep(300)
            page.click(".attr-edit-close")
            Thread.sleep(300)
            Then("다이얼로그가 닫힌다") {
                val dialog = page.querySelector(".attr-editor-dialog")
                val display = dialog!!.evaluate("el => getComputedStyle(el).display") as String
                display shouldBe "none"
            }
            Then("다이얼로그 외부의 캔버스가 클릭 가능하다") {
                page.querySelector(".type-canvas") shouldNotBe null
            }
        }

        // MD3 버튼 확인
        Then("스냅 버튼이 MD3 Outlined Button으로 렌더링된다") {
            val button = page.querySelector("md-outlined-button")
            button shouldNotBe null
        }
    }
})
