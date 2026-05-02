package dev.sayaya.handbook.domain

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class AttributeTypeTest : StringSpec({

    "Text 타입은 올바르게 단순화된다" {
        val type = AttributeType.text()
        type.simplify() shouldBe "text"
    }

    "Array 타입은 중첩된 요소를 포함하여 올바르게 단순화된다" {
        val type = AttributeType.array(AttributeType.text())
        type.simplify() shouldBe "text[]"
    }

    "Document 타입은 참조 타입명을 반환한다" {
        val type = AttributeType()
        type.type("document")
        type.referencedType("Customer")
        type.simplify() shouldBe "Customer"
    }
})
