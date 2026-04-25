package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("agent.html")
internal class AgentEdgeCaseTest: GwtTestSpec({
    Given("에이전트 UI 엣지 케이스") {
        // 빈 진행률 이벤트 (0/0)
        When("그룹 0/0인 진행률 이벤트를 수신하면") {
            page.evaluate("""
                (function() {
                    var detail = 'AGENT_COMMAND:{"type":"progress","payload":{"currentGroup":0,"totalGroups":0,"parallel":false,"stepCount":0}}';
                    var evt = new CustomEvent('handbook-workspace-event', {detail: detail, bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(300)
            Then("에이전트 UI가 에러 없이 유지된다") {
                page.querySelector("body")!!.innerHTML().isNotBlank() shouldBe true
            }
        }

        // 아티팩트 없는 complete
        When("아티팩트 없이 complete 이벤트를 수신하면") {
            page.evaluate("""
                (function() {
                    var detail = 'AGENT_COMMAND:{"type":"complete","payload":{"intent":"test","executionId":"e1"}}';
                    var evt = new CustomEvent('handbook-workspace-event', {detail: detail, bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(500)
            Then("아티팩트 패널이 표시되지 않는다") {
                val panel = page.querySelector(".agent-artifact-panel")
                if (panel != null) {
                    val display = panel.evaluate("el => getComputedStyle(el).display") as String
                    display shouldBe "none"
                }
            }
        }

        // malformed AGENT_COMMAND
        When("malformed AGENT_COMMAND를 수신하면") {
            page.evaluate("""
                (function() {
                    var evt = new CustomEvent('handbook-workspace-event', {detail: 'AGENT_COMMAND:not_json', bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(300)
            Then("UI가 에러 없이 유지된다") {
                page.querySelector("body")!!.innerHTML().isNotBlank() shouldBe true
            }
        }

        // 아티팩트 변경 0건
        When("변경 0건인 아티팩트 complete를 수신하면") {
            page.evaluate("""
                (function() {
                    var detail = 'AGENT_COMMAND:{"type":"complete","payload":{"intent":"empty","executionId":"e2","artifact":{"summary":"Nothing changed","changes":[]}}}';
                    var evt = new CustomEvent('handbook-workspace-event', {detail: detail, bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(500)
            Then("아티팩트 패널에 변경 행이 0개이다") {
                val panel = page.querySelector(".agent-artifact-panel")
                if (panel != null) {
                    val rows = panel.querySelectorAll(".agent-artifact-change-row")
                    rows.count() shouldBe 0
                }
            }
        }
    }
})
