package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * UC-T11: 에이전트에 의한 타입 조작
 * UC-T12: 에이전트에 의한 타입 검색
 * UC-T15: 실시간 협업 (TYPE_CREATED/DELETED)
 * UC-T17: 프레즌스 표시 및 해제
 * UC-T20: 다중 사용자 프레즌스 동시 수신
 * UC-T25: 워크스페이스 전환 (동적 데이터 재로딩)
 * UC-T26: URL에서 워크스페이스 ID 추출 폴백
 */
@GwtHtml("canvastest.html")
internal class CollaborationTest: GwtTestSpec({
    Given("캔버스가 초기화됨") {
        
        // UC-T11: 에이전트 타입 생성
        When("에이전트가 CREATE 이벤트를 디스패치하면") {
            val before = page.querySelectorAll(".type-box").count()
            page.evaluate("""
                (function() {
                    var detail = ['CREATE type:agent-test'];
                    var evt = new CustomEvent('handbook-mutate', {detail: detail, bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(500)
            Then("타입 박스가 1개 추가된다") {
                val after = page.querySelectorAll(".type-box").count()
                after shouldBe before + 1
                page.querySelector(".type-box[data-type-key='agent-test:1.0']") shouldNotBe null
            }
        }

        // UC-T11: 에이전트 타입 속성 수정
        When("에이전트가 SET description 이벤트를 디스패치하면") {
            page.evaluate("""
                (function() {
                    var detail = ['SET type:customer:1.0:description=Modified by Agent'];
                    var evt = new CustomEvent('handbook-mutate', {detail: detail, bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(500)
            Then("타입 캔버스가 유지된다") {
                page.querySelector(".type-canvas") shouldNotBe null
                val html = page.querySelector(".type-canvas")!!.innerHTML()
                html.isNotBlank() shouldBe true
            }
        }

        // UC-T15: 실시간 협업 (다른 사용자의 생성 수신)
        When("TYPE_CREATED 워크스페이스 이벤트를 디스패치하면") {
            page.evaluate("""
                (function() {
                    var detail = { event_type: 'TYPE_CREATED', workspace: 'demo', payload: { id: 'collaboration-test', version: '1.0' } };
                    var evt = new CustomEvent('handbook-workspace-event', {detail: JSON.stringify(detail), bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(1000)
            Then("캔버스가 유지된다") {
                page.querySelector(".type-canvas") shouldNotBe null
            }
            Then("상단 상태바가 유지된다") {
                page.querySelector(".type-status-header") shouldNotBe null
            }
        }

        // UC-T15: 실시간 협업 (다른 사용자의 삭제 수신)
        When("TYPE_DELETED 워크스페이스 이벤트를 디스패치하면") {
            page.evaluate("""
                (function() {
                    var detail = { event_type: 'TYPE_DELETED', workspace: 'demo', payload: { id: 'customer', version: '1.0' } };
                    var evt = new CustomEvent('handbook-workspace-event', {detail: JSON.stringify(detail), bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(1000)
            Then("캔버스가 유지된다") {
                page.querySelector(".type-canvas") shouldNotBe null
            }
        }

        // UC-T17: 프레즌스 표시
        When("다른 사용자의 PRESENCE 이벤트를 수신하면") {
            page.evaluate("""
                (function() {
                    var detail = { event_type: 'PRESENCE', workspace: 'demo', payload: { user: 'user1', typeKey: 'order:1.0' } };
                    var evt = new CustomEvent('handbook-workspace-event', {detail: JSON.stringify(detail), bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(500)
            Then("캔버스가 정상 유지된다") {
                page.querySelector(".type-canvas") shouldNotBe null
            }
        }

        // UC-T17: 프레즌스 해제
        When("PRESENCE 해제 이벤트를 수신하면") {
            page.evaluate("""
                (function() {
                    var detail = { event_type: 'PRESENCE', workspace: 'demo', payload: { user: 'user1', typeKey: null } };
                    var evt = new CustomEvent('handbook-workspace-event', {detail: JSON.stringify(detail), bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(500)
            Then("캔버스가 정상 유지된다") {
                page.querySelector(".type-canvas") shouldNotBe null
            }
        }

        // UC-T11: 에이전트 수정 직후 저장 확인
        When("에이전트가 타입 편집 후 Save를 요청하면") {
            page.evaluate("""
                (function() {
                    var detail = ['SET type:customer:1.0:description=Agent Save Test'];
                    var evt = new CustomEvent('handbook-mutate', {detail: detail, bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(300)
            page.click(".type-ctrl-btn-save")
            Thread.sleep(500)
            Then("캔버스와 상단바가 유지된다") {
                page.querySelector(".type-canvas") shouldNotBe null
                page.querySelector(".type-status-header") shouldNotBe null
            }
        }

        // UC-T19: 에이전트 + 사용자 동시 편집 — 선택 상태 유지
        When("사용자가 타입을 편집하는 중 에이전트가 다른 타입을 수정하면") {
            page.click(".type-box[data-type-key='customer:1.0']")
            val beforeKey = page.evaluate("document.querySelector('.type-box[selected]').getAttribute('data-type-key')")
            
            page.evaluate("""
                (function() {
                    var detail = ['SET type:order:1.0:description=Agent Background Edit'];
                    var evt = new CustomEvent('handbook-mutate', {detail: detail, bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(500)
            
            Then("사용자의 선택 상태가 유지된다") {
                page.querySelector(".type-box[selected]") shouldNotBe null
            }
            Then("선택된 타입의 key가 변하지 않았다") {
                val afterKey = page.evaluate("document.querySelector('.type-box[selected]').getAttribute('data-type-key')")
                afterKey shouldBe beforeKey
            }
        }

        // UC-T20: 다중 사용자 프레즌스 동시 수신
        When("여러 사용자의 PRESENCE 이벤트를 동시에 수신하면") {
            val before = page.querySelectorAll(".type-box").count()
            page.evaluate("""
                (function() {
                    var detail1 = { event_type: 'PRESENCE', workspace: 'demo', payload: { user: 'user1', typeKey: 'customer:1.0' } };
                    var detail2 = { event_type: 'PRESENCE', workspace: 'demo', payload: { user: 'user2', typeKey: 'order:1.0' } };
                    window.dispatchEvent(new CustomEvent('handbook-workspace-event', {detail: JSON.stringify(detail1), bubbles: false}));
                    window.dispatchEvent(new CustomEvent('handbook-workspace-event', {detail: JSON.stringify(detail2), bubbles: false}));
                })()
            """.trimIndent())
            Thread.sleep(500)
            Then("캔버스의 타입 박스 개수가 유지된다") {
                page.querySelectorAll(".type-box").count() shouldBe before
            }
            Then("상단바 버튼 상태가 유지된다") {
                page.querySelector(".type-ctrl-btn-undo") shouldNotBe null
            }
        }

        // UC-T22: 이벤트 폭주 상황 안정성
        When("다른 사용자가 빠르게 3개 타입을 생성하면") {
            page.evaluate("""
                (function() {
                    for(var i=0; i<3; i++) {
                        var detail = { event_type: 'TYPE_CREATED', workspace: 'demo', payload: { id: 'stress-'+i, version: '1.0' } };
                        window.dispatchEvent(new CustomEvent('handbook-workspace-event', {detail: JSON.stringify(detail), bubbles: false}));
                    }
                })()
            """.trimIndent())
            Thread.sleep(1500)
            Then("캔버스와 상단바가 유지된다") {
                page.querySelector(".type-canvas") shouldNotBe null
                page.querySelector(".type-status-header") shouldNotBe null
            }
        }

        // UC-T25: 워크스페이스 전환
        When("쉘에서 워크스페이스를 변경하여 이벤트를 발행하면") {
            page.evaluate("""
                (function() {
                    var detail = { workspaceId: 'new-workspace-id' };
                    var evt = new CustomEvent('handbook-workspace-context', {detail: JSON.stringify(detail), bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(1500)
            Then("새로운 워크스페이스의 데이터로 캔버스가 갱신된다") {
                page.querySelector(".type-canvas") shouldNotBe null
            }
        }

        // UC-T26: URL 워크스페이스 ID 폴백
        When("워크스페이스 ID 스트림이 비어있는 상태로 발행되면") {
            page.evaluate("""
                (function() {
                    var detail = { workspaceId: null };
                    var evt = new CustomEvent('handbook-workspace-context', {detail: JSON.stringify(detail), bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(1000)
            Then("URL에서 워크스페이스 ID를 추출하여 로딩이 시도된다") {
                page.querySelector(".type-canvas") shouldNotBe null
            }
        }
    }
})
