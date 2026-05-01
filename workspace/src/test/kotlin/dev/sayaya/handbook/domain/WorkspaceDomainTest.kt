package dev.sayaya.handbook.domain

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec

@GwtHtml("workspaceTest.html")
class WorkspaceDomainTest : GwtTestSpec({
    beforeTest {
        Thread.sleep(1000)
    }

    Given("Workspace 도메인 모델 (공용)") {
        Then("모든 빌더와 필드가 정상 작동해야 한다") {
            page shouldContainLog "LOG_WS_RESULT:ws-1:My WS"
            page shouldContainLog "LOG_USER_RESULT:u-1:U1"
            page shouldContainLog "LOG_GROUP_RESULT:g-1:ws-1"
            page shouldContainLog "WORKSPACE_TEST_READY"
        }
    }
})
