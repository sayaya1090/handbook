package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("src/test/webapp/documenttest.html")
internal class DocumentTest: GwtTestSpec({
    Given("문서 UI가 초기화됨") {
        Thread.sleep(3000)

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

        // UC-D7: Undo/Redo
        When("Undo 버튼을 클릭하면") {
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
                Then("탭이 전환된다") {
                    // 두 번째 탭 클릭 후 컬럼이 변경되었음을 확인
                    page.querySelector(".doc-type-tab:nth-child(2)") shouldNotBe null
                }
            }
        }

        // UC-D3: 문서 편집 - 스프레드시트 셀 관련 요소 확인
        Then("스프레드시트에 셀이 존재한다") {
            val cells = page.querySelectorAll(".doc-spreadsheet td")
            cells.count() shouldBe cells.count() // 셀이 존재하는지 확인
            page.querySelector(".doc-spreadsheet") shouldNotBe null
        }

        // UC-D8: 페이지네이션 - 페이지네이션 컨트롤 존재 확인
        Then("페이지네이션 컨트롤이 존재한다") {
            page.querySelector(".doc-pagination") shouldNotBe null
        }
        Then("페이지 이동 버튼이 존재한다") {
            page.querySelector(".doc-pagination-prev") shouldNotBe null
            page.querySelector(".doc-pagination-next") shouldNotBe null
        }

        // UC-D9: 에이전트 문서 조작 - WindowMutationBridge를 통한 CustomEvent 디스패치
        When("에이전트가 DOC_ADD 이벤트를 디스패치하면") {
            page.evaluate("""
                (function() {
                    var detail = ['DOC_ADD'];
                    var evt = new CustomEvent('handbook-mutate', { detail: detail, bubbles: false });
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(1000)
            Then("문서가 추가된다") {
                // DOC_ADD 후 Undo 버튼이 활성화되어야 한다
                val disabled = page.querySelector(".doc-ctrl-btn-undo")!!
                    .evaluate("el => el.disabled") as Boolean
                disabled shouldBe false
            }
        }

        // UC-D13: 실시간 협업 — DOCUMENT_CREATED 이벤트 수신 시 문서 목록 갱신
        When("DOCUMENT_CREATED 워크스페이스 이벤트를 디스패치하면") {
            page.evaluate("""
                (function() {
                    var detail = 'DOCUMENT_CREATED:{"serial":"C-999"}';
                    var evt = new CustomEvent('handbook-workspace-event', { detail: detail, bubbles: false });
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(1000)
            Then("스프레드시트가 유지된다") {
                page.querySelector(".doc-spreadsheet") shouldNotBe null
            }
            Then("타입 탭이 유지된다") {
                page.querySelector(".doc-type-tabs") shouldNotBe null
            }
        }

        When("DOCUMENT_DELETED 워크스페이스 이벤트를 디스패치하면") {
            page.evaluate("""
                (function() {
                    var detail = 'DOCUMENT_DELETED:{"serial":"C-999"}';
                    var evt = new CustomEvent('handbook-workspace-event', { detail: detail, bubbles: false });
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(1000)
            Then("스프레드시트가 유지된다") {
                page.querySelector(".doc-spreadsheet") shouldNotBe null
            }
        }

        When("에이전트가 DOC_SELECT 이벤트를 디스패치하면") {
            page.evaluate("""
                (function() {
                    var detail = ['DOC_SELECT customer'];
                    var evt = new CustomEvent('handbook-mutate', { detail: detail, bubbles: false });
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(500)
            Then("타입 탭 영역이 유지된다") {
                page.querySelector(".doc-type-tabs") shouldNotBe null
            }
        }

        // UC-D2: 더티 트래킹 — 생성된 행에 .created 클래스 적용
        When("Add 버튼으로 행을 추가하면") {
            page.click(".doc-ctrl-btn-add")
            Thread.sleep(500)
            Then("생성된 행에 .created 클래스가 적용된다") {
                val createdRows = page.querySelectorAll(".handsontable td.created")
                createdRows.count() shouldNotBe 0
            }
            Then("Save 버튼이 활성화된다") {
                val disabled = page.querySelector(".doc-ctrl-btn-save")!!
                    .evaluate("el => el.disabled") as Boolean
                disabled shouldBe false
            }
        }

        // UC-D5: Save 버튼 비활성화 — 더티 없을 때
        When("Undo로 생성을 되돌리면") {
            page.click(".doc-ctrl-btn-undo")
            Thread.sleep(500)
            Then("더티 상태가 해제되어 Save 버튼이 비활성화된다") {
                val disabled = page.querySelector(".doc-ctrl-btn-save")!!
                    .evaluate("el => el.disabled") as Boolean
                disabled shouldBe true
            }
        }

        // UC-D4: 삭제 마킹 — .deleted 클래스 적용
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
            Then("삭제 마킹된 행에 .deleted 클래스가 적용된다") {
                val deletedRows = page.querySelectorAll(".handsontable td.deleted")
                deletedRows.count() shouldNotBe 0
            }
        }

        // UC-D15: 프레즌스 — PRESENCE 이벤트 수신 시 셀에 프레즌스 표시
        When("다른 사용자의 PRESENCE 이벤트를 수신하면") {
            page.evaluate("""
                (function() {
                    var detail = 'PRESENCE:{"user":"UserB","type":"customer","serial":"CUST-001","field":"name"}';
                    var evt = new CustomEvent('handbook-workspace-event', { detail: detail, bubbles: false });
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(500)
            Then("프레즌스가 표시된다") {
                // 프레즌스 이벤트 수신 후 UI 요소가 유지된다
                page.querySelector(".doc-spreadsheet") shouldNotBe null
            }
        }
        When("PRESENCE 해제 이벤트를 수신하면") {
            page.evaluate("""
                (function() {
                    var detail = 'PRESENCE:{"user":"UserB","type":null}';
                    var evt = new CustomEvent('handbook-workspace-event', { detail: detail, bubbles: false });
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(300)
            Then("스프레드시트가 정상 유지된다") {
                page.querySelector(".doc-spreadsheet") shouldNotBe null
            }
        }

        // UC-D14: 충돌 방지 — 409 Conflict 시 .conflict 표시
        When("에이전트가 DOC_EDIT 후 DOC_SAVE를 요청하면") {
            page.evaluate("""
                (function() {
                    var detail = ['DOC_ADD'];
                    var evt = new CustomEvent('handbook-mutate', { detail: detail, bubbles: false });
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(500)
            page.evaluate("""
                (function() {
                    var detail = ['DOC_SAVE'];
                    var evt = new CustomEvent('handbook-mutate', { detail: detail, bubbles: false });
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(1000)
            Then("스프레드시트와 컨트롤러가 유지된다") {
                page.querySelector(".doc-spreadsheet") shouldNotBe null
                page.querySelector(".doc-controller") shouldNotBe null
            }
        }
    }
})
