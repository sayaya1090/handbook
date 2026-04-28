package dev.sayaya.handbook.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.*

class WorkspaceTest : BehaviorSpec({
    Given("Workspace 도메인") {
        When("생성하면") {
            val id = UUID.randomUUID().toString()
            val workspace = Workspace.create(id, "Test Workspace", "Desc")
            Then("필드가 정상적으로 할당되어야 한다") {
                workspace.id() shouldBe id
                workspace.name() shouldBe "Test Workspace"
                workspace.description() shouldBe "Desc"
            }
        }
        When("이름이 비어있으면") {
            Then("IllegalArgumentException이 발생해야 한다") {
                shouldThrow<IllegalArgumentException> {
                    Workspace.create("id", " ", "desc")
                }
            }
        }
    }
    Given("WorkspaceSimple") {
        When("생성하면") {
            val id = UUID.randomUUID().toString()
            val simple = Workspace.WorkspaceSimple.create(id, "Simple WS")
            Then("필드가 정상 할당된다") {
                simple.id() shouldBe id
                simple.name() shouldBe "Simple WS"
            }
        }
        When("ID가 비어있으면") {
            Then("IllegalArgumentException이 발생해야 한다") {
                shouldThrow<IllegalArgumentException> {
                    Workspace.WorkspaceSimple.create("", "name")
                }
            }
        }
    }
})
