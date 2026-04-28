package dev.sayaya.handbook.domain

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class SchemaApiDomainTest : BehaviorSpec({
    Given("TypeValue DTO") {
        When("생성하면") {
            val type = TypeValue.create("test-type", "1.0", 0, 0)
            Then("필드가 정상적으로 할당된다") {
                type.id() shouldBe "test-type"
                type.version() shouldBe "1.0"
            }
        }
    }
})
