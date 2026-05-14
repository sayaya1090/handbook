package dev.sayaya.handbook.usecase

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe

@GwtHtml("agent_bridge_test.html")
class AgentBridgeTest : GwtTestSpec({
    Given("Agent Bridge Communication") {
        Then("에이전트 관련 모든 브릿지 통신이 정상 작동해야 한다") {
            page shouldContainLog "LOG_AGENT_MUTATION_RECEIVED:test-change"
            page shouldContainLog "LOG_WS_ID_RECEIVED:ws-123"
            page shouldContainLog "LOG_CURRENT_WS_ID:ws-123"
            page shouldContainLog "LOG_SEARCH_RESULT:RESULT:find-me"
            page shouldContainLog "LOG_STATE_RESULT:MOCK_STATE"
            page shouldContainLog "AGENT_BRIDGE_TEST_READY"
        }
        
        Then("publishId 호출 시 전역 window 속성에 값이 저장되어야 한다") {
            val globalVal = page.evaluate("window.__handbook_workspace_id__") as String
            globalVal shouldBe "ws-123"
        }
    }
})
