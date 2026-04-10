package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain

@GwtHtml("src/test/webapp/documenttest.html")
internal class DocumentTest: GwtTestSpec({
    Given("문서 UI가 초기화됨") {
        // UC-D1: 문서 조회
        Then("컨테이너가 존재한다") {
            page.querySelector(".doc-container") shouldNotBe null
        }
        Then("컨트롤러 툴바가 존재한다") {
            page.querySelector(".doc-controller") shouldNotBe null
        }
        Then("타입 탭이 존재한다") {
            page.querySelector(".doc-type-tabs") shouldNotBe null
        }
        Then("타입 탭 2개가 렌더링된다") {
            val tabs = page.querySelectorAll(".doc-type-tab")
            tabs.count() shouldBe 2
        }
        Then("스프레드시트가 존재한다") {
            page.querySelector(".doc-spreadsheet") shouldNotBe null
        }

        // UC-D2: 문서 생성
        Then("Add 버튼이 존재한다") {
            page.querySelector(".doc-ctrl-btn-add") shouldNotBe null
        }
        When("Add 버튼을 클릭하면") {
            page.click(".doc-ctrl-btn-add")
            Thread.sleep(500)
            Then("Undo 버튼이 활성화된다") {
                val disabled = page.querySelector(".doc-ctrl-btn-undo")!!
                    .evaluate("el => el.disabled") as Boolean
                disabled shouldBe false
            }
        }

        // UC-D4: 문서 삭제
        Then("Delete 버튼이 존재한다") {
            page.querySelector(".doc-ctrl-btn-delete") shouldNotBe null
        }

        // UC-D5: 저장
        Then("Save 버튼이 존재한다") {
            page.querySelector(".doc-ctrl-btn-save") shouldNotBe null
        }

        // UC-D7: Undo/Redo 버튼 초기 상태
        Then("Redo 버튼이 존재한다") {
            page.querySelector(".doc-ctrl-btn-redo") shouldNotBe null
        }

        // UC-D6: 타입 전환
        When("두 번째 타입 탭을 클릭하면") {
            val tabs = page.querySelectorAll(".doc-type-tab")
            if (tabs.count() >= 2) {
                page.click(".doc-type-tab:nth-child(2)")
                Thread.sleep(500)
                Then("탭 클릭 후 스프레드시트가 유지된다") {
                    page.querySelector(".doc-spreadsheet") shouldNotBe null
                }
            }
        }

        // UC-D3: 문서 편집 - 스프레드시트 셀 관련 요소 확인
        Then("스프레드시트에 셀이 존재한다") {
            val cells = page.querySelectorAll(".doc-spreadsheet td")
            cells.count() shouldBeGreaterThan 0
        }

        // UC-D8: 페이지네이션 - 페이지네이션 컨트롤 존재 확인
        Then("페이지네이션 컨트롤이 존재한다") {
            page.querySelector(".doc-pagination") shouldNotBe null
        }
        Then("페이지 이동 버튼이 존재한다") {
            page.querySelector(".doc-page-prev") shouldNotBe null
            page.querySelector(".doc-page-next") shouldNotBe null
        }

        // UC-D4: 삭제 마킹 — Delete 실행 후 UI 유지
        When("행을 선택 후 Delete 버튼을 클릭하면") {
            // 첫 번째 행 선택
            page.evaluate("""
                (function() {
                    var td = document.querySelector('.handsontable td');
                    if (td) td.click();
                })()
            """.trimIndent())
            Thread.sleep(300)
            page.click(".doc-ctrl-btn-delete")
            Thread.sleep(500)
            Then("삭제 실행 후 스프레드시트가 유지된다") {
                page.querySelector(".doc-spreadsheet") shouldNotBe null
            }
        }
    }
})
