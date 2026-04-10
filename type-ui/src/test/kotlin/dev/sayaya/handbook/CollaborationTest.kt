package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("src/test/webapp/canvastest.html")
internal class CollaborationTest: GwtTestSpec({
    Given("캔버스가 초기화됨") {
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

        // UC-T15: 실시간 협업 — TYPE_CREATED 이벤트 수신 시 캔버스 유지 검증
        When("TYPE_CREATED 워크스페이스 이벤트를 디스패치하면") {
            page.evaluate("""
                (function() {
                    var detail = 'TYPE_CREATED:{"id":"new-type-001"}';
                    var evt = new CustomEvent('handbook-workspace-event', { detail: detail, bubbles: false });
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(1000)
            Then("캔버스가 유지된다") {
                page.querySelector(".type-canvas") shouldNotBe null
            }
            Then("컨트롤러 툴바가 유지된다") {
                page.querySelector(".type-controller") shouldNotBe null
            }
        }

        When("TYPE_DELETED 워크스페이스 이벤트를 디스패치하면") {
            page.evaluate("""
                (function() {
                    var detail = 'TYPE_DELETED:{"id":"new-type-001"}';
                    var evt = new CustomEvent('handbook-workspace-event', { detail: detail, bubbles: false });
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(1000)
            Then("캔버스가 유지된다") {
                page.querySelector(".type-canvas") shouldNotBe null
            }
        }

        // UC-T17: 프레즌스 — PRESENCE 이벤트 수신 시 타입 박스에 프레즌스 표시
        When("다른 사용자의 PRESENCE 이벤트를 수신하면") {
            page.evaluate("""
                (function() {
                    var detail = 'PRESENCE:{"user":"UserB","typeKey":"customer:1.0"}';
                    var evt = new CustomEvent('handbook-workspace-event', { detail: detail, bubbles: false });
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(500)
            Then("캔버스가 정상 유지된다") {
                page.querySelector(".type-canvas") shouldNotBe null
            }
        }
        When("PRESENCE 해제 이벤트를 수신하면") {
            page.evaluate("""
                (function() {
                    var detail = 'PRESENCE:{"user":"UserB","typeKey":null}';
                    var evt = new CustomEvent('handbook-workspace-event', { detail: detail, bubbles: false });
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(300)
            Then("캔버스가 정상 유지된다") {
                page.querySelector(".type-canvas") shouldNotBe null
            }
        }

        // UC-T16: 충돌 방지 — 에이전트 편집 후 Save
        When("에이전트가 타입 편집 후 Save를 요청하면") {
            page.evaluate("""
                (function() {
                    var detail = ['CREATE type:conflict-test'];
                    var evt = new CustomEvent('handbook-mutate', { detail: detail, bubbles: false });
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(500)
            Then("캔버스와 컨트롤러가 유지된다") {
                page.querySelector(".type-canvas") shouldNotBe null
                page.querySelector(".type-controller") shouldNotBe null
            }
        }

        // UC-T19: 에이전트 + 사용자 동시 편집 — 사용자 선택 상태 유지
        When("사용자가 타입을 편집하는 중 에이전트가 다른 타입을 수정하면") {
            val boxCountBefore = page.querySelectorAll(".type-box").count()
            page.click(".type-box[data-type-key='customer:1.0']")
            Thread.sleep(200)
            val firstBoxKey = page.querySelector(".type-box[data-type-key='customer:1.0']")!!.getAttribute("data-type-key")
            page.evaluate("""
                (function() {
                    var detail = ['SET type:order:1.0:attributes=[{"name":"priority","type":"number"}]'];
                    var evt = new CustomEvent('handbook-mutate', {detail: detail, bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(500)
            Then("사용자의 선택 상태가 유지된다") {
                val selected = page.querySelector(".type-box[data-type-key='customer:1.0']")!!.getAttribute("selected")
                selected shouldNotBe null
            }
            Then("선택된 타입의 key가 변하지 않았다") {
                val afterKey = page.querySelector(".type-box[data-type-key='customer:1.0']")!!.getAttribute("data-type-key")
                afterKey shouldBe firstBoxKey
            }
            Then("타입 박스 개수가 변하지 않았다") {
                val boxCountAfter = page.querySelectorAll(".type-box").count()
                boxCountAfter shouldBe boxCountBefore
            }
        }

        // UC-T20: 다중 사용자 프레즌스 동시 표시
        When("여러 사용자의 PRESENCE 이벤트를 동시에 수신하면") {
            val boxCountBefore = page.querySelectorAll(".type-box").count()
            page.evaluate("""
                (function() {
                    ['UserB:customer:1.0', 'UserC:order:1.0', 'UserD:customer:1.0'].forEach(function(p) {
                        var parts = p.split(':');
                        var detail = 'PRESENCE:{"user":"' + parts[0] + '","userName":"' + parts[0] + '","type":"' + parts[1] + '","serial":null,"field":null}';
                        var evt = new CustomEvent('handbook-workspace-event', {detail: detail, bubbles: false});
                        window.dispatchEvent(evt);
                    });
                })()
            """.trimIndent())
            Thread.sleep(500)
            Then("캔버스의 타입 박스 개수가 유지된다") {
                page.querySelectorAll(".type-box").count() shouldBe boxCountBefore
            }
            Then("컨트롤러 버튼 상태가 유지된다") {
                page.querySelector(".type-controller") shouldNotBe null
                page.querySelectorAll(".type-ctrl-group").count() shouldNotBe 0
            }
            Then("SVG 화살표가 유지된다") {
                page.querySelector(".box-reference-svg") shouldNotBe null
            }
        }

        // UC-T22: TYPE_CREATED 이벤트 연속 수신 (다른 사용자가 빠르게 여러 타입 생성)
        When("다른 사용자가 빠르게 3개 타입을 생성하면") {
            val boxesBefore = page.querySelectorAll(".type-box").count()
            page.evaluate("""
                (function() {
                    for(var i=1; i<=3; i++) {
                        var detail = 'TYPE_CREATED:{"id":"rapid-'+i+'"}';
                        var evt = new CustomEvent('handbook-workspace-event', {detail: detail, bubbles: false});
                        window.dispatchEvent(evt);
                    }
                })()
            """.trimIndent())
            Thread.sleep(1000)
            Then("캔버스와 컨트롤러가 유지된다") {
                page.querySelector(".type-canvas") shouldNotBe null
                page.querySelector(".type-controller") shouldNotBe null
            }
            Then("에러로 인한 빈 캔버스가 아니다") {
                val canvasHtml = page.querySelector(".type-canvas")!!.innerHTML()
                canvasHtml.isNotBlank() shouldBe true
            }
            Then("SVG 오버레이가 유지된다") {
                page.querySelector(".box-reference-svg") shouldNotBe null
            }
        }
        // UC-T23: 사용자가 편집 중인 타입을 다른 사용자가 수정 (TYPE_CREATED with same id)
        When("사용자가 편집 중인 타입과 같은 id로 TYPE_CREATED 이벤트가 수신되면") {
            val boxesBefore = page.querySelectorAll(".type-box").count()
            page.click(".type-box[data-type-key='customer:1.0']")
            Thread.sleep(200)
            val selectedKey = page.querySelector(".type-box[data-type-key='customer:1.0']")!!.getAttribute("data-type-key")
            page.evaluate("""
                (function() {
                    var detail = 'TYPE_CREATED:{"id":"customer"}';
                    var evt = new CustomEvent('handbook-workspace-event', {detail: detail, bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(1000)
            Then("캔버스의 타입 박스 개수가 유지된다") {
                page.querySelectorAll(".type-box").count() shouldBe boxesBefore
            }
            Then("선택된 타입의 key가 유지된다") {
                val afterKey = page.querySelector(".type-box[data-type-key='customer:1.0']")!!.getAttribute("data-type-key")
                afterKey shouldBe selectedKey
            }
            Then("캔버스 HTML이 비어있지 않다") {
                val html = page.querySelector(".type-canvas")!!.innerHTML()
                html.isNotBlank() shouldBe true
            }
            Then("컨트롤러가 유지된다") {
                page.querySelector(".type-controller") shouldNotBe null
                page.querySelector(".type-ctrl-btn-save") shouldNotBe null
            }
        }

        // UC-T24: 사용자가 편집 중인 타입을 다른 사용자가 삭제 (TYPE_DELETED with same id)
        When("사용자가 편집 중인 타입과 같은 id로 TYPE_DELETED 이벤트가 수신되면") {
            val boxesBefore = page.querySelectorAll(".type-box").count()
            page.click(".type-box[data-type-key='customer:1.0']")
            Thread.sleep(200)
            val selectedKey = page.querySelector(".type-box[data-type-key='customer:1.0']")!!.getAttribute("data-type-key")
            page.evaluate("""
                (function() {
                    var detail = 'TYPE_DELETED:{"id":"customer"}';
                    var evt = new CustomEvent('handbook-workspace-event', {detail: detail, bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(1000)
            Then("캔버스가 정상 유지된다") {
                page.querySelector(".type-canvas") shouldNotBe null
                val html = page.querySelector(".type-canvas")!!.innerHTML()
                html.isNotBlank() shouldBe true
            }
            Then("컨트롤러 툴바가 유지된다") {
                page.querySelector(".type-controller") shouldNotBe null
                page.querySelector(".type-ctrl-btn-add") shouldNotBe null
                page.querySelector(".type-ctrl-btn-save") shouldNotBe null
            }
            Then("SVG 오버레이가 유지된다") {
                page.querySelector(".box-reference-svg") shouldNotBe null
            }
        }
    }
})
