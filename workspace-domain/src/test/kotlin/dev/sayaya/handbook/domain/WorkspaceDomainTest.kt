package dev.sayaya.handbook.domain

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe

@GwtHtml("workspace_domain.html")
class WorkspaceDomainTest : GwtTestSpec({
    Given("Workspace 도메인 빌더") {
        Then("Workspace 객체가 올바르게 생성되어야 한다") {
            page shouldContainLog "LOG_WS_RESULT:ws-1:My Workspace"
        }
    }
    Given("User 도메인 빌더") {
        Then("User 객체가 올바르게 생성되어야 한다") {
            page shouldContainLog "LOG_USER_RESULT:u-1:User One"
        }
    }
    Given("Group 도메인 빌더") {
        Then("Group 객체가 올바르게 생성되어야 한다") {
            page shouldContainLog "LOG_GROUP_RESULT:g-1:ws-1"
        }
    }
})
