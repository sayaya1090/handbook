package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain

@GwtHtml("agent.html")
internal class AgentProgressTest: GwtTestSpec({
    Given("에이전트 UI가 준비됨") {
        // UC-A7: 그룹 레벨 진행률 표시
        When("Progress Group 버튼을 클릭하면") {
            page.click("#btn-progress-group")
            Thread.sleep(500)
            Then("프로그레스 바가 표시된다") {
                val progress = page.querySelector(".progress-container")
                progress shouldNotBe null
                val display = progress!!.evaluate("el => getComputedStyle(el).display") as String
                display shouldNotBe "none"
            }
            Then("프로그레스 라벨에 그룹 정보가 표시된다") {
                val label = page.querySelector(".progress-label")
                label shouldNotBe null
                val text = label!!.textContent() ?: ""
                text.contains("2/5") shouldBe true
            }
        }

        // UC-A8: 아티팩트 요약 패널
        When("Complete Artifact 버튼을 클릭하면") {
            page.click("#btn-complete-artifact")
            Thread.sleep(500)
            Then("아티팩트 요약 패널이 표시된다") {
                val panel = page.querySelector(".agent-artifact-panel")
                panel shouldNotBe null
                val display = panel!!.evaluate("el => getComputedStyle(el).display") as String
                display shouldNotBe "none"
            }
            Then("아티팩트 요약 텍스트가 표시된다") {
                val summary = page.querySelector(".agent-artifact-summary")
                summary shouldNotBe null
                summary!!.textContent()!!.contains("3개 필드") shouldBe true
            }
            Then("변경 항목 3개가 렌더링된다") {
                val rows = page.querySelectorAll(".agent-artifact-change-row")
                rows.count() shouldBe 3
            }
            Then("닫기 버튼을 클릭하면 패널이 숨겨진다") {
                page.click(".agent-artifact-close")
                Thread.sleep(300)
                val panel = page.querySelector(".agent-artifact-panel")
                val display = panel!!.evaluate("el => getComputedStyle(el).display") as String
                display shouldBe "none"
            }
        }

        // UC-A8: 완료 토스트 with 변경 건수
        When("Complete Artifact 버튼을 다시 클릭하면") {
            Thread.sleep(5000) // 이전 토스트가 사라질 때까지 대기
            page.click("#btn-complete-artifact")
            Thread.sleep(500)
            Then("완료 토스트에 변경 건수가 표시된다") {
                val toasts = page.querySelectorAll(".ui-toast-success")
                toasts.count() shouldBe 1
                val toastText = toasts.first().textContent() ?: ""
                toastText.contains("3") shouldBe true
            }
        }

        // UC-A13: 서브 에이전트 진행률 이벤트 수신
        When("서브 에이전트 진행률 이벤트를 수신하면") {
            page.evaluate("""
                (function() {
                    var detail = 'AGENT_COMMAND:{"type":"progress","payload":{"currentGroup":1,"totalGroups":3,"subAgentName":"analyzer","subAgentIndex":1,"subAgentTotal":2,"parallel":false,"stepCount":1}}';
                    var evt = new CustomEvent('handbook-workspace-event', {detail: detail, bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(500)
            Then("진행률이 표시된다") {
                val progress = page.querySelector(".progress-container")
                progress shouldNotBe null
                val display = progress!!.evaluate("el => getComputedStyle(el).display") as String
                display shouldNotBe "none"
                val label = page.querySelector(".progress-label")
                label shouldNotBe null
                val text = label!!.textContent() ?: ""
                // 진행률 라벨에 숫자가 포함되어야 한다 (예: "1/3", "1/2")
                text.contains(Regex("\\d")) shouldBe true
            }
        }
    }
})
