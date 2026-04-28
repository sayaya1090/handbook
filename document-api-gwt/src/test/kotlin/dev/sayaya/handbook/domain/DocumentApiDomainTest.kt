package dev.sayaya.handbook.domain

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import jsinterop.base.JsPropertyMap

class DocumentApiDomainTest : BehaviorSpec({
    Given("DocumentValue DTO") {
        When("생성하면") {
            val doc = DocumentValue()
            doc.id = "doc1"
            doc.data = JsPropertyMap.of("title", "Hello")
            Then("필드가 정상적으로 할당된다") {
                doc.id shouldBe "doc1"
                doc.data.get("title") shouldBe "Hello"
            }
        }
    }
})
