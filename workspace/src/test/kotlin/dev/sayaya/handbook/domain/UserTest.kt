package dev.sayaya.handbook.domain

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.util.*

class UserTest : BehaviorSpec({
    Given("User 도메인") {
        When("생성하면") {
            val id = UUID.randomUUID().toString()
            val user = User.create(id, "User One", "user@example.com")
            Then("필드가 정상적으로 할당되어야 한다") {
                user.id() shouldBe id
                user.name() shouldBe "User One"
                user.email() shouldBe "user@example.com"
            }
        }
    }
})
