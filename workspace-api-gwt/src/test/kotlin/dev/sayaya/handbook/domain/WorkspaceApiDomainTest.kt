package dev.sayaya.handbook.domain

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.util.UUID

class WorkspaceApiDomainTest : BehaviorSpec({
    Given("Workspace DTO") {
        When("생성하면") {
            val id = UUID.randomUUID().toString()
            val workspace = Workspace()
            workspace.id = id
            workspace.name = "Test Workspace"
            Then("필드가 정상적으로 할당된다") {
                workspace.id() shouldBe id
                workspace.name() shouldBe "Test Workspace"
            }
        }
    }
})
