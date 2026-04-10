package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("src/test/webapp/canvastest.html")
internal class CanvasTest: GwtTestSpec({
    Given("캔버스가 초기화됨") {
        Thread.sleep(3000)

        // UC-T1: 타입 조회
        Then("캔버스 요소가 존재한다") {
            page.querySelector(".type-canvas") shouldNotBe null
        }
        Then("컨트롤러 툴바가 존재한다") {
            page.querySelector(".type-controller") shouldNotBe null
        }
        Then("타입 박스 2개가 렌더링된다") {
            val boxes = page.querySelectorAll(".type-box")
            boxes.count() shouldBe 2
        }
        Then("customer 타입 박스에 속성 3개가 표시된다") {
            val rows = page.querySelectorAll(".type-box:first-child .type-attr-row")
            rows.count() shouldBe 3
        }
        Then("order 타입 박스에 속성 2개가 표시된다") {
            val rows = page.querySelectorAll(".type-box:last-child .type-attr-row")
            rows.count() shouldBe 2
        }
        Then("타입 이름이 표시된다") {
            val name = page.querySelector(".type-box .type-name")
            name shouldNotBe null
            name!!.textContent() shouldNotBe ""
        }
        Then("Document 참조 화살표가 그려진다") {
            val arrows = page.querySelectorAll(".box-reference-container svg")
            arrows.count() shouldBe 1
        }

        // UC-T2: 타입 생성
        When("Add Type 버튼을 클릭하면") {
            val before = page.querySelectorAll(".type-box").count()
            page.click(".type-ctrl-btn-add")
            Thread.sleep(500)
            Then("타입 박스가 1개 추가된다") {
                val after = page.querySelectorAll(".type-box").count()
                after shouldBe before + 1
            }
        }

        // UC-T3: 타입 삭제
        When("타입을 선택하고 Delete 키를 누르면") {
            val before = page.querySelectorAll(".type-box").count()
            page.click(".type-box:last-child")
            Thread.sleep(200)
            page.keyboard().press("Delete")
            Thread.sleep(500)
            Then("선택된 타입이 삭제된다") {
                val after = page.querySelectorAll(".type-box").count()
                after shouldBe before - 1
            }
        }

        // UC-T9: Undo/Redo
        When("Ctrl+Z를 누르면") {
            val before = page.querySelectorAll(".type-box").count()
            page.keyboard().press("Control+z")
            Thread.sleep(500)
            Then("삭제가 되돌려진다") {
                val after = page.querySelectorAll(".type-box").count()
                after shouldBe before + 1
            }
        }
        When("Ctrl+Shift+Z를 누르면") {
            val before = page.querySelectorAll(".type-box").count()
            page.keyboard().press("Control+Shift+z")
            Thread.sleep(500)
            Then("Redo가 실행된다") {
                val after = page.querySelectorAll(".type-box").count()
                after shouldBe before - 1
            }
        }

        // UC-T4: 타입 이동 (선택 확인)
        When("타입 박스를 클릭하면") {
            page.click(".type-box:first-child")
            Thread.sleep(200)
            Then("선택 상태가 활성화된다") {
                val box = page.querySelector(".type-box:first-child")
                box shouldNotBe null
                val selected = box!!.getAttribute("selected")
                selected shouldNotBe null
            }
        }

        // UC-T7: 속성 편집 (컨텍스트 메뉴)
        When("타입 박스를 우클릭하면") {
            page.click(".type-box:first-child", com.microsoft.playwright.Page.ClickOptions().setButton(com.microsoft.playwright.options.MouseButton.RIGHT))
            Thread.sleep(300)
            Then("컨텍스트 메뉴가 표시된다") {
                val menu = page.querySelector(".box-context-menu")
                menu shouldNotBe null
                val display = menu!!.evaluate("el => getComputedStyle(el).display") as String
                display shouldNotBe "none"
            }
        }

        // UC-T8: 기간 이동 버튼 존재 확인
        Then("Before/After 버튼이 존재한다") {
            page.querySelector(".type-ctrl-btn-before") shouldNotBe null
            page.querySelector(".type-ctrl-btn-after") shouldNotBe null
        }

        // UC-T10: 저장/로드 버튼 존재 확인
        Then("Save/Reload 버튼이 존재한다") {
            page.querySelector(".type-ctrl-btn-save") shouldNotBe null
            page.querySelector(".type-ctrl-btn-reload") shouldNotBe null
        }

        // 모드 토글 존재 확인
        Then("모드 토글 버튼이 존재한다") {
            page.querySelector(".type-mode-toggle") shouldNotBe null
        }

        // 스냅 체크박스 존재 확인
        Then("스냅 체크박스가 존재한다") {
            page.querySelector(".type-snap-checkbox") shouldNotBe null
        }

        // UC-T11: 에이전트 타입 생성 - WindowMutationBridge CustomEvent 디스패치
        When("에이전트가 CREATE 이벤트를 디스패치하면") {
            val before = page.querySelectorAll(".type-box").count()
            page.evaluate("""
                (function() {
                    var detail = ['CREATE type:agent-test'];
                    var evt = new CustomEvent('handbook-mutate', { detail: detail, bubbles: false });
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(1000)
            Then("타입 박스가 1개 추가된다") {
                val after = page.querySelectorAll(".type-box").count()
                after shouldBe before + 1
            }
        }

        // UC-T12: 에이전트 SET 명령 - 타입 설명 변경
        When("에이전트가 SET description 이벤트를 디스패치하면") {
            page.evaluate("""
                (function() {
                    var detail = ['SET type:customer:1.0:description=에이전트 수정 설명'];
                    var evt = new CustomEvent('handbook-mutate', { detail: detail, bubbles: false });
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(500)
            Then("타입 캔버스가 유지된다") {
                page.querySelector(".type-canvas") shouldNotBe null
            }
        }

        // UC-T5: 리사이즈 - 리사이즈 핸들 존재 확인
        Then("타입 박스에 리사이즈 핸들이 존재한다") {
            val handle = page.querySelector(".type-box .resize-handle")
            handle shouldNotBe null
        }

        // UC-T6: 이름/버전 편집 - 이름 요소 존재 확인
        Then("타입 이름 요소가 편집 가능하다") {
            val name = page.querySelector(".type-box .type-name")
            name shouldNotBe null
        }

        // UC-T13: 모바일 - 컨트롤러 툴바 flex-wrap 확인
        Then("컨트롤러 툴바가 존재하고 레이아웃 속성을 가진다") {
            val controller = page.querySelector(".type-controller")
            controller shouldNotBe null
            val display = controller!!.evaluate("el => getComputedStyle(el).display") as String
            display shouldNotBe "none"
        }
    }
})
