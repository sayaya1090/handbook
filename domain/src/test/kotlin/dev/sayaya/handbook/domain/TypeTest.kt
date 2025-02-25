package dev.sayaya.handbook.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import java.time.Instant

class TypeTest : StringSpec({
    val now = Instant.now()
    "Type은 부모와 자식 관계가 올바르게 설정된다" {
        val parentType = Type(
            id = "parent-type",
            version = "1.0",
            effectDateTime = now,
            expireDateTime = Instant.MAX,
            parent = null,
            description = "The parent type",
            attributes = listOf(Attribute.Companion.ValueAttribute(name = "Attr1", inherited = false)),
            primitive = false
        )

        val childType = Type(
            id = "child-type",
            version = "1.0",
            effectDateTime = now,
            expireDateTime = Instant.MAX,
            parent = parentType.id,
            description = "The child type",
            attributes = listOf(Attribute.Companion.MapAttribute(name = "MapAttr", inherited = false)),
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
                version = "1.0",
                effectDateTime = now,
                expireDateTime = Instant.MAX,
                parent = null,
                description = "Invalid blank id",
                attributes = emptyList(),
                primitive = false
            )
        }
        exceptionForBlank shouldHaveMessage "Type id cannot be blank"
    }
    "Type은 종료일시가 시작일시보다 나중이어야 함" {
        shouldThrow<IllegalArgumentException> {
            Type(
                id = "test-type",
                version = "1.0",
                effectDateTime = now.plusSeconds(3600),
                expireDateTime = now,
                parent = null,
                description = "Invalid date time test",
                attributes = emptyList(),
                primitive = false
            )
        }.shouldHaveMessage("Expire date time must be after effect date time")
    }

    "Type은 같은 시작일시와 종료일시를 허용하지 않는다" {
        shouldThrow<IllegalArgumentException> {
            Type(
                id = "test-type",
                version = "1.0",
                effectDateTime = now,
                expireDateTime = now,  // 같은 시간
                parent = null,
                description = "Same date time test",
                attributes = emptyList(),
                primitive = false
            )
        }.shouldHaveMessage("Expire date time must be after effect date time")
    }
})
