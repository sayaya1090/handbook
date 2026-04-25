package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("agent.html")
internal class AgentSearchVisualizationTest: GwtTestSpec({
    Given("에이전트 UI가 준비됨") {
        // UC-A11: 검색 시각화 — search 커맨드로 시각적 오케스트레이션
        When("Search 버튼을 클릭하면") {
            page.click("#btn-search")
            Thread.sleep(500)
            Then("프로그레스가 표시된다") {
                val progress = page.querySelector(".progress-container")
                progress shouldNotBe null
                val display = progress!!.evaluate("el => getComputedStyle(el).display") as String
                display shouldNotBe "none"
            }
            Then("프로그레스 라벨에 검색 쿼리가 포함된다") {
                val label = page.querySelector(".progress-label")
                label shouldNotBe null
                val text = label!!.textContent() ?: ""
                text.contains("customer") shouldBe true
            }
        }

        // UC-A11: 검색 시각화 — 커스텀 이벤트로 search 커맨드 수신
        When("SSE를 통해 search 커맨드가 수신되면") {
            page.evaluate("""
                (function() {
                    var detail = 'AGENT_COMMAND:{"type":"search","navigateTo":"/workspace/ws-1/type","query":"order","targets":["#target-element"],"summary":"order 타입 1건 발견"}';
                    var evt = new CustomEvent('handbook-workspace-event', {detail: detail, bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(500)
            Then("에이전트 UI가 정상 유지된다") {
                page.querySelector(".agent-input-container") shouldNotBe null
                page.querySelector(".agent-input-field") shouldNotBe null
                page.querySelector(".agent-input-send") shouldNotBe null
            }
            Then("프로그레스 라벨에 검색 쿼리가 포함된다") {
                val label = page.querySelector(".progress-label")
                label shouldNotBe null
                val text = label!!.textContent() ?: ""
                text.contains("order") shouldBe true
            }
        }
    }
})
