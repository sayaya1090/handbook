package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * UC-A14: DELEGATE 커맨드 수신 시 UI 안정성
 * UC-A15: 다중 에이전트 연속 완료 + 아티팩트 패널
 */
@GwtHtml("agenttest.html")
internal class AgentCollaborationTest: GwtTestSpec({
    Given("에이전트 UI가 준비됨") {
        // UC-A14: DELEGATE 커맨드 수신
        When("DELEGATE 커맨드를 수신하면") {
            page.evaluate("""
                (function() {
                    var detail = 'AGENT_COMMAND:{"type":"delegate","payload":{"subAgentName":"writer","task":"문서 작성"}}';
                    var evt = new CustomEvent('handbook-workspace-event', {detail: detail, bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(300)
            Then("에이전트 UI가 정상 유지된다") {
                page.querySelector(".agent-input-container") shouldNotBe null
                page.querySelector(".agent-input-field") shouldNotBe null
                page.querySelector(".agent-input-send") shouldNotBe null
                // body HTML이 비어있지 않아야 한다
                val bodyHtml = page.querySelector("body")!!.innerHTML()
                bodyHtml.isNotBlank() shouldBe true
            }
        }

        // UC-A15: 다중 에이전트 완료 — 연속 아티팩트
        When("두 에이전트가 연속으로 완료되면") {
            page.evaluate("""
                (function() {
                    for(var i=1; i<=2; i++) {
                        var detail = 'AGENT_COMMAND:{"type":"complete","payload":{"intent":"task-'+i+'","executionId":"exec-'+i+'","artifact":{"summary":"Result '+i+'","changes":[{"type":"CREATE","target":"t'+i+'","description":"d'+i+'"}]}}}';
                        var evt = new CustomEvent('handbook-workspace-event', {detail: detail, bubbles: false});
                        window.dispatchEvent(evt);
                    }
                })()
            """.trimIndent())
            Thread.sleep(1000)
            Then("아티팩트 패널이 표시된다") {
                val panel = page.querySelector(".agent-artifact-panel")
                panel shouldNotBe null
            }
        }

        // Complete 커맨드
        When("Complete 커맨드 수신 시") {
            page.click("#btn-complete")
            Thread.sleep(500)
            Then("전송 버튼이 다시 표시된다") {
                val sendBtn = page.querySelector(".agent-input-send")
                sendBtn shouldNotBe null
            }
        }
    }
})
