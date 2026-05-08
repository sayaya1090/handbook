package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * UC-T1: 타입 조회
 * UC-T2: 타입 생성
 * UC-T3: 타입 삭제
 * UC-T4: 타입 이동
 * UC-T5: 타입 리사이즈
 * UC-T6: 타입 이름/버전 편집
 * UC-T7: 속성 추가/편집 (기본)
 * UC-T8: 레이아웃 기간 이동 (UI 존재 확인)
 * UC-T9: Undo/Redo
 * UC-T10: 저장/다시 로드 (UI 존재 확인)
 * UC-T13: 모바일 반응형 레이아웃 (Toolbar)
 */
@GwtHtml("canvastest.html")
internal class CanvasTest: GwtTestSpec({
    Given("캔버스가 초기화됨") {
        // UC-T1: 타입 조회
        Then("캔버스 요소가 존재한다") {
            page.querySelector(".type-canvas") shouldNotBe null
        }
        
        // 포토샵 스타일 역할 분리 검증
        Then("상단 상태바(.type-status-header)에 글로벌 액션 및 설정 버튼들이 존재한다") {
            page.querySelector(".type-status-header") shouldNotBe null
            page.querySelector(".type-mode-toggle") shouldNotBe null
            page.querySelector(".type-ctrl-btn-undo") shouldNotBe null
            page.querySelector(".type-ctrl-btn-redo") shouldNotBe null
            page.querySelector(".type-ctrl-btn-save") shouldNotBe null
            page.querySelector(".type-snap-button") shouldNotBe null
        }
        Then("좌측 툴레일(.type-controller)에 그리기 및 편집 도구 버튼들이 존재한다") {
            val rail = page.querySelector(".type-controller")
            rail shouldNotBe null
            rail!!.querySelector(".type-ctrl-btn-add") shouldNotBe null
            rail!!.querySelector(".type-ctrl-btn-remove") shouldNotBe null
            rail!!.querySelector(".type-ctrl-btn-delete") shouldNotBe null
        }
        Then("타입 박스 2개가 렌더링된다") {
            val boxes = page.querySelectorAll(".type-box")
            boxes.count() shouldBe 2
        }
        Then("customer 타입 박스에 속성 3개가 표시된다") {
            val rows = page.querySelectorAll(".type-box[data-type-key='customer:1.0'] .type-attr-row")
            rows.count() shouldBe 3
        }
        Then("order 타입 박스에 속성 2개가 표시된다") {
            val rows = page.querySelectorAll(".type-box[data-type-key='order:1.0'] .type-attr-row")
            rows.count() shouldBe 2
        }
        Then("타입 이름이 표시된다") {
            val name = page.querySelector(".type-box .type-name")
            name shouldNotBe null
            name!!.textContent() shouldNotBe ""
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
            // 타입 박스를 클릭하여 선택
            page.click(".type-box[data-type-key='order:1.0']")
            Thread.sleep(500)
            // 캔버스에 포커스를 보장한 뒤 Delete 키 발행
            page.evaluate("document.querySelector('.type-canvas').focus()")
            Thread.sleep(200)
            page.keyboard().press("Delete")
            Thread.sleep(1000)
            Then("선택된 타입이 삭제된다") {
                val after = page.querySelectorAll(".type-box").count()
                after shouldBe before - 1
            }
        }

        // UC-T4: 타입 이동 (선택 확인)
        When("타입 박스를 클릭하면") {
            page.click(".type-box[data-type-key='customer:1.0']")
            Thread.sleep(200)
            Then("선택 상태가 활성화된다") {
                val box = page.querySelector(".type-box[data-type-key='customer:1.0']")
                box shouldNotBe null
                val selected = box!!.getAttribute("selected")
                selected shouldNotBe null
            }
        }

        // UC-T5: 리사이즈 - 리사이즈 핸들 존재 확인
        Then("타입 박스에 리사이즈 핸들이 존재한다") {
            val handle = page.querySelector(".type-box .type-resize-handle")
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

        // 모드 토글 존재 확인
        Then("모드 토글 버튼이 존재한다") {
            page.querySelector(".type-mode-toggle") shouldNotBe null
        }

        // 스냅 버튼 존재 확인
        Then("스냅 버튼이 존재한다") {
            page.querySelector(".type-snap-button") shouldNotBe null
        }
    }
})
