package dev.sayaya.handbook.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage

class TypeTest : StringSpec({
    "Type은 부모와 자식 관계가 올바르게 설정된다" {
        val parentType = Type(
            id = "parent-type",
            parent = null,
            description = "The parent type",
            attributes = listOf(Attribute.Companion.ValueAttribute(name = "Attr1")),
            primitive = false
        )

        val childType = Type(
            id = "child-type",
            parent = parentType.id,
            description = "The child type",
            attributes = listOf(Attribute.Companion.MapAttribute(name = "MapAttr")),
            primitive = false
        )

        parentType.id shouldBe "parent-type"
        childType.parent shouldBe parentType.id
        childType.attributes.shouldNotBeEmpty()
    }
    
    "Type은 빈 id를 허용하지 않는다" {
        val exceptionForBlank = shouldThrow<IllegalArgumentException> {
            Type(
                id = "",
                parent = null,
                description = "Invalid blank id",
                attributes = emptyList(),
                primitive = false
            )
        }
        exceptionForBlank shouldHaveMessage "Type id cannot be blank"
    }
})