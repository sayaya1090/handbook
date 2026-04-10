package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("src/test/webapp/documenttest.html")
internal class DocumentCollaborationTest: GwtTestSpec({
    Given("문서 UI가 초기화됨") {
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

        // UC-D13: 실시간 협업 — DOCUMENT_CREATED 이벤트 수신 시 문서 목록 갱신
        When("DOCUMENT_CREATED 워크스페이스 이벤트를 디스패치하면") {
            val cellsBefore = page.querySelectorAll(".doc-spreadsheet td").count()
            val colsBefore = page.querySelectorAll(".handsontable thead th").count()
            page.evaluate("""
                (function() {
                    var detail = 'DOCUMENT_CREATED:{"serial":"C-999"}';
                    var evt = new CustomEvent('handbook-workspace-event', { detail: detail, bubbles: false });
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(1000)
            Then("스프레드시트 셀 수가 유지된다") {
                val cellsAfter = page.querySelectorAll(".doc-spreadsheet td").count()
                cellsAfter shouldBe cellsBefore
            }
            Then("컬럼 수가 유지된다") {
                val colsAfter = page.querySelectorAll(".handsontable thead th").count()
                colsAfter shouldBe colsBefore
            }
            Then("타입 탭 수가 유지된다") {
                val tabs = page.querySelectorAll(".doc-type-tab")
                tabs.count() shouldBe 2
            }
        }

        When("DOCUMENT_DELETED 워크스페이스 이벤트를 디스패치하면") {
            val cellsBefore = page.querySelectorAll(".doc-spreadsheet td").count()
            page.evaluate("""
                (function() {
                    var detail = 'DOCUMENT_DELETED:{"serial":"C-999"}';
                    var evt = new CustomEvent('handbook-workspace-event', { detail: detail, bubbles: false });
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(1000)
            Then("스프레드시트 셀 수가 유지된다") {
                val cellsAfter = page.querySelectorAll(".doc-spreadsheet td").count()
                cellsAfter shouldBe cellsBefore
            }
            Then("컨트롤러 툴바가 유지된다") {
                page.querySelector(".doc-controller") shouldNotBe null
                val addBtn = page.querySelector(".doc-ctrl-btn-add")
                addBtn shouldNotBe null
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
                // 툴바 버튼들이 존재하고 활성/비활성 상태를 가진다
                val addBtn = page.querySelector(".doc-ctrl-btn-add")
                addBtn shouldNotBe null
                val saveBtn = page.querySelector(".doc-ctrl-btn-save")
                saveBtn shouldNotBe null
                val undoBtn = page.querySelector(".doc-ctrl-btn-undo")
                undoBtn shouldNotBe null
                // Add 버튼은 항상 활성화 상태
                val addDisabled = addBtn!!.evaluate("el => el.disabled") as Boolean
                addDisabled shouldBe false
            }
        }

        // UC-D15: 프레즌스 — PRESENCE 이벤트 수신 시 셀에 프레즌스 표시
        When("다른 사용자의 PRESENCE 이벤트를 수신하면") {
            val cellsBefore = page.querySelectorAll(".doc-spreadsheet td").count()
            val colsBefore = page.querySelectorAll(".handsontable thead th").count()
            page.evaluate("""
                (function() {
                    var detail = 'PRESENCE:{"user":"UserB","type":"customer","serial":"CUST-001","field":"name"}';
                    var evt = new CustomEvent('handbook-workspace-event', { detail: detail, bubbles: false });
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(500)
            Then("프레즌스 수신 후 스프레드시트 셀 수가 유지된다") {
                val cellsAfter = page.querySelectorAll(".doc-spreadsheet td").count()
                cellsAfter shouldBe cellsBefore
            }
            Then("프레즌스 수신 후 컬럼 수가 유지된다") {
                val colsAfter = page.querySelectorAll(".handsontable thead th").count()
                colsAfter shouldBe colsBefore
            }
        }
        When("PRESENCE 해제 이벤트를 수신하면") {
            val cellsBefore = page.querySelectorAll(".doc-spreadsheet td").count()
            page.evaluate("""
                (function() {
                    var detail = 'PRESENCE:{"user":"UserB","type":null}';
                    var evt = new CustomEvent('handbook-workspace-event', { detail: detail, bubbles: false });
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(300)
            Then("스프레드시트 셀 수가 변하지 않는다") {
                val cellsAfter = page.querySelectorAll(".doc-spreadsheet td").count()
                cellsAfter shouldBe cellsBefore
            }
            Then("컨트롤러 버튼들이 유지된다") {
                page.querySelector(".doc-ctrl-btn-add") shouldNotBe null
                page.querySelector(".doc-ctrl-btn-delete") shouldNotBe null
                page.querySelector(".doc-ctrl-btn-save") shouldNotBe null
                page.querySelector(".doc-ctrl-btn-undo") shouldNotBe null
                page.querySelector(".doc-ctrl-btn-redo") shouldNotBe null
            }
        }

        // UC-D17: 에이전트 + 사용자 동시 문서 편집
        When("사용자가 셀을 편집하는 중 에이전트가 DOC_ADD를 실행하면") {
            val colsBefore = page.querySelectorAll(".handsontable thead th").count()
            // 셀 클릭으로 선택 (checkbox=0, serial=1, effective=2, expire=3, name=4)
            val td = page.querySelector(".doc-spreadsheet td:nth-child(5)")
            if (td != null) td.click()
            Thread.sleep(200)
            page.evaluate("""
                (function() {
                    var detail = ['DOC_ADD'];
                    var evt = new CustomEvent('handbook-mutate', {detail: detail, bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(500)
            Then("스프레드시트 컬럼 수가 유지된다") {
                val colsAfter = page.querySelectorAll(".handsontable thead th").count()
                colsAfter shouldBe colsBefore
            }
            Then("스프레드시트 HTML이 비어있지 않다") {
                val html = page.querySelector(".doc-spreadsheet")!!.innerHTML()
                html.isNotBlank() shouldBe true
            }
        }

        // UC-D18: 다중 사용자 프레즌스 — 같은 문서 다른 필드
        When("두 사용자가 같은 문서의 다른 필드를 편집하면") {
            val cellsBefore = page.querySelectorAll(".doc-spreadsheet td").count()
            val colsBefore = page.querySelectorAll(".handsontable thead th").count()
            page.evaluate("""
                (function() {
                    var events = [
                        'PRESENCE:{"user":"UserB","userName":"UserB","type":"customer","serial":"CUST-001","field":"name"}',
                        'PRESENCE:{"user":"UserC","userName":"UserC","type":"customer","serial":"CUST-001","field":"age"}'
                    ];
                    events.forEach(function(d) {
                        var evt = new CustomEvent('handbook-workspace-event', {detail: d, bubbles: false});
                        window.dispatchEvent(evt);
                    });
                })()
            """.trimIndent())
            Thread.sleep(500)
            Then("스프레드시트 셀 수가 유지된다") {
                val cellsAfter = page.querySelectorAll(".doc-spreadsheet td").count()
                cellsAfter shouldBe cellsBefore
            }
            Then("컬럼 수가 유지된다") {
                val colsAfter = page.querySelectorAll(".handsontable thead th").count()
                colsAfter shouldBe colsBefore
            }
        }

        // UC-D19: DOCUMENT_CREATED 연속 수신
        When("다른 사용자가 빠르게 5개 문서를 생성하면") {
            page.evaluate("""
                (function() {
                    for(var i=1; i<=5; i++) {
                        var detail = 'DOCUMENT_CREATED:{"serial":"RAPID-'+i+'","type":"customer"}';
                        var evt = new CustomEvent('handbook-workspace-event', {detail: detail, bubbles: false});
                        window.dispatchEvent(evt);
                    }
                })()
            """.trimIndent())
            Thread.sleep(1000)
            Then("스프레드시트와 컨트롤러가 유지된다") {
                page.querySelector(".doc-spreadsheet") shouldNotBe null
                page.querySelector(".doc-controller") shouldNotBe null
                // 툴바 버튼들이 모두 존재한다
                page.querySelector(".doc-ctrl-btn-add") shouldNotBe null
                page.querySelector(".doc-ctrl-btn-delete") shouldNotBe null
                page.querySelector(".doc-ctrl-btn-save") shouldNotBe null
                page.querySelector(".doc-ctrl-btn-undo") shouldNotBe null
                page.querySelector(".doc-ctrl-btn-redo") shouldNotBe null
                // 스프레드시트 HTML이 비어있지 않다
                val html = page.querySelector(".doc-spreadsheet")!!.innerHTML()
                html.isNotBlank() shouldBe true
            }
        }

        // UC-D20: 에이전트 문서 편집 + 다른 사용자 삭제 이벤트 동시
        When("에이전트가 DOC_EDIT하는 중 다른 사용자가 문서를 삭제하면") {
            val colsBefore = page.querySelectorAll(".handsontable thead th").count()
            page.evaluate("""
                (function() {
                    var mutate = new CustomEvent('handbook-mutate', {detail: ['DOC_ADD'], bubbles: false});
                    window.dispatchEvent(mutate);
                    var del = new CustomEvent('handbook-workspace-event', {
                        detail: 'DOCUMENT_DELETED:{"serial":"CUST-002","type":"customer"}', bubbles: false
                    });
                    window.dispatchEvent(del);
                })()
            """.trimIndent())
            Thread.sleep(1000)
            Then("스프레드시트 컬럼 수가 유지된다") {
                val colsAfter = page.querySelectorAll(".handsontable thead th").count()
                colsAfter shouldBe colsBefore
            }
            Then("컨트롤러 툴바가 유지된다") {
                page.querySelector(".doc-controller") shouldNotBe null
                page.querySelector(".doc-ctrl-btn-add") shouldNotBe null
            }
        }

        // UC-D21: 사용자가 편집 중인 문서를 다른 사용자가 수정
        When("사용자가 편집 중인 문서와 같은 serial로 DOCUMENT_CREATED 이벤트가 수신되면") {
            val cellsBefore = page.querySelectorAll(".doc-spreadsheet td").count()
            val colsBefore = page.querySelectorAll(".handsontable thead th").count()
            // 현재 편집 중인 문서의 serial로 DOCUMENT_CREATED 수신 (다른 사용자가 같은 문서를 수정)
            page.evaluate("""
                (function() {
                    var detail = 'DOCUMENT_CREATED:{"serial":"CUST-001","type":"customer"}';
                    var evt = new CustomEvent('handbook-workspace-event', {detail: detail, bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(1000)
            Then("스프레드시트 셀 수가 유지된다") {
                val cellsAfter = page.querySelectorAll(".doc-spreadsheet td").count()
                cellsAfter shouldBe cellsBefore
            }
            Then("컬럼 수가 유지된다") {
                val colsAfter = page.querySelectorAll(".handsontable thead th").count()
                colsAfter shouldBe colsBefore
            }
            Then("타입 탭이 유지된다") {
                val tabs = page.querySelectorAll(".doc-type-tab")
                tabs.count() shouldBe 2
            }
        }

        // UC-D22: 사용자가 편집 중인 문서를 다른 사용자가 삭제
        When("사용자가 편집 중인 문서의 serial로 DOCUMENT_DELETED 이벤트가 수신되면") {
            val colsBefore = page.querySelectorAll(".handsontable thead th").count()
            page.evaluate("""
                (function() {
                    var detail = 'DOCUMENT_DELETED:{"serial":"CUST-001","type":"customer"}';
                    var evt = new CustomEvent('handbook-workspace-event', {detail: detail, bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(1000)
            Then("스프레드시트가 정상 유지된다") {
                page.querySelector(".doc-spreadsheet") shouldNotBe null
                val html = page.querySelector(".doc-spreadsheet")!!.innerHTML()
                html.isNotBlank() shouldBe true
            }
            Then("컬럼 수가 유지된다") {
                val colsAfter = page.querySelectorAll(".handsontable thead th").count()
                colsAfter shouldBe colsBefore
            }
            Then("컨트롤러 툴바 버튼들이 유지된다") {
                page.querySelector(".doc-ctrl-btn-add") shouldNotBe null
                page.querySelector(".doc-ctrl-btn-delete") shouldNotBe null
                page.querySelector(".doc-ctrl-btn-save") shouldNotBe null
            }
        }
    }
})
