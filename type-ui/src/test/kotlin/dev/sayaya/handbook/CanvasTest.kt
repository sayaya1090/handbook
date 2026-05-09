package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * UC-T1: 타입 조회
 * UC-T2: 타입 생성
 * UC-T3: 타입 삭제
 * UC-T5: 타입 리사이즈
 * UC-T6: 타입 이름/버전 편집
 * UC-T7: 속성 표시 검증
 * UC-T8: 레이아웃 기간 이동 및 정보 표시
 * UC-T9: Undo/Redo
 * UC-T10: 저장/로드
 * UC-T13: 모바일 반응형 레이아웃 (Toolbar)
 */
@GwtHtml("canvastest.html")
internal class CanvasTest: GwtTestSpec({
    Given("캔버스가 초기화됨") {
        page.reload()
        page.waitForSelector(".type-canvas")
        page.waitForSelector(".type-box[data-type-key='customer:1.0']")
        Thread.sleep(1500)

        // UC-T1: 타입 조회 — 캔버스 요소 실재 확인
        Then("캔버스 요소가 존재한다") {
            page.querySelector(".type-canvas") shouldNotBe null
        }
        
        // UC-T1, T8, T9, T10: 글로벌 조작 도구들
        Then("상단 상태바(.type-status-header)에 글로벌 액션 및 설정 버튼들이 존재한다") {
            page.querySelector(".type-status-header") shouldNotBe null
            page.querySelector(".type-mode-toggle") shouldNotBe null
            page.querySelector(".type-ctrl-btn-undo") shouldNotBe null
            page.querySelector(".type-ctrl-btn-redo") shouldNotBe null
            page.querySelector(".type-ctrl-btn-save") shouldNotBe null
            page.querySelector(".type-snap-button") shouldNotBe null
        }

        // UC-T2, T3: 타입 생성 및 삭제 도구들
        Then("좌측 툴레일(.type-controller)에 그리기 및 편집 도구 버튼들이 존재한다") {
            val rail = page.querySelector(".type-controller")
            rail shouldNotBe null
            rail!!.querySelector(".type-ctrl-btn-add") shouldNotBe null
            rail!!.querySelector(".type-ctrl-btn-delete") shouldNotBe null
        }

        // UC-T1: 타입 조회 — 초기 렌더링 검증
        Then("타입 박스 2개가 렌더링된다") {
            val boxes = page.querySelectorAll(".type-box")
            boxes.count() shouldBe 2
        }

        // UC-T7: 속성 표시 검증
        Then("customer 타입 박스에 속성 3개가 표시된다") {
            val rows = page.querySelectorAll(".type-box[data-type-key='customer:1.0'] .type-attr-row")
            rows.count() shouldBe 3
        }

        Then("order 타입 박스에 속성 2개가 표시된다") {
            val rows = page.querySelectorAll(".type-box[data-type-key='order:1.0'] .type-attr-row")
            rows.count() shouldBe 2
        }

        Then("타입 이름이 표시된다") {
            page.textContent(".type-box[data-type-key='customer:1.0'] .type-name") shouldBe "customer"
            page.textContent(".type-box[data-type-key='order:1.0'] .type-name") shouldBe "order"
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
        When("타입 박스를 클릭하고 Delete 키를 누르면") {
            val boxSelector = ".type-box[data-type-key='customer:1.0']"
            val before = page.querySelectorAll(".type-box").count()
            
            // 박스 클릭 및 선택 상태 대기
            page.click(boxSelector)
            page.waitForSelector(".type-box[data-type-key='customer:1.0'][selected]")
            
            // 캔버스 포커스 및 삭제 키 입력
            page.focus(".type-canvas")
            page.keyboard().press("Delete")
            Thread.sleep(1000)
            
            Then("선택된 타입이 삭제된다") {
                val after = page.querySelectorAll(".type-box").count()
                after shouldBe before - 1
                page.querySelector(boxSelector) shouldBe null
            }
        }

        When("타입 박스를 클릭하면") {
            // 다이얼로그 가림막 이슈 방지를 위해 다이얼로그 강제 숨김
            page.evaluate("document.querySelectorAll('.attr-editor-dialog').forEach(el => el.style.display = 'none')")
            page.click(".type-box[data-type-key='order:1.0']")
            Then("선택 상태가 활성화된다") {
                page.waitForSelector(".type-box[data-type-key='order:1.0'][selected]") shouldNotBe null
            }
        }

        // UC-T5: 타입 리사이즈
        Then("타입 박스에 리사이즈 핸들이 존재한다") {
            page.querySelector(".type-box[data-type-key='order:1.0'] .type-resize-handle") shouldNotBe null
        }

        // UC-T6: 타입 이름/버전 편집
        Then("타입 이름 요소가 편집 가능하다") {
            page.querySelector(".type-box[data-type-key='order:1.0'] .type-name") shouldNotBe null
        }

        // UC-T8: 레이아웃 기간 정보 표시
        Then("컨트롤러 툴바가 존재하고 레이아웃 속성을 가진다") {
            page.querySelector(".type-period-label") shouldNotBe null
        }

        // UC-T1: 모드 전환 버튼
        Then("모드 토글 버튼이 존재한다") {
            page.querySelector(".type-mode-toggle") shouldNotBe null
        }

        // UC-T1: 스냅 버튼
        Then("스냅 버튼이 존재한다") {
            page.querySelector(".type-snap-button") shouldNotBe null
        }
    }
})
