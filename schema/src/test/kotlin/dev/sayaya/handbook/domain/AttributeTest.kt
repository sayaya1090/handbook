package dev.sayaya.handbook.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage

class AttributeTest : StringSpec({

    "Attribute는 올바른 값이 주어지면 성공적으로 생성된다" {
        val attribute = Attribute.create(
            "attr-1",
            "author",
            1,
            AttributeType.text()
        )
        attribute.name() shouldBe "author"
        attribute.type().type() shouldBe "text"
    }

    "Attribute는 유효하지 않은 이름으로 생성 시 예외를 발생시킨다" {
        shouldThrow<IllegalArgumentException> {
            Attribute.create("id", " ", 1, AttributeType.text())
        } shouldHaveMessage "Attribute name cannot be blank."

        shouldThrow<IllegalArgumentException> {
            Attribute.create("id", "invalid name!", 1, AttributeType.text())
        } shouldHaveMessage "Attribute name can only contain alphanumerics, hyphens, and underscores."
    }
})
