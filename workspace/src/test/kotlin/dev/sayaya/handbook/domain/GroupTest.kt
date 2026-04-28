package dev.sayaya.handbook.domain

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.util.*

class GroupTest : BehaviorSpec({
    Given("Group 도메인") {
        When("생성하면") {
            val id = UUID.randomUUID().toString()
            val workspaceId = UUID.randomUUID().toString()
            val group = Group.create(id, workspaceId, "Group One", "Desc")
            Then("필드가 정상적으로 할당되어야 한다") {
                group.id shouldBe id
                group.workspace shouldBe workspaceId
                group.name shouldBe "Group One"
                group.description shouldBe "Desc"
            }
        }
    }
})
