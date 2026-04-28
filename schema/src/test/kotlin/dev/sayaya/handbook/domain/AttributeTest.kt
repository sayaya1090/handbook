package dev.sayaya.handbook.domain

import com.fasterxml.jackson.annotation.JsonInclude
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.module.kotlin.kotlinModule
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.throwable.shouldHaveMessage

class AttributeTest : StringSpec({

    val objectMapper: ObjectMapper = JsonMapper.builder()
        .addModule(kotlinModule())
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
        .changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_EMPTY) }
        .build()

    "Attribute는 올바른 값이 주어지면 성공적으로 생성된다" {
        val attribute = Attribute.create(
            "attr-1",
            "author",
            1,
            AttributeType.text()
        )
        attribute.name shouldBe "author"
        attribute.type.type shouldBe "text"
    }

    "Attribute는 유효하지 않은 이름으로 생성 시 예외를 발생시킨다" {
        shouldThrow<IllegalArgumentException> {
            Attribute.create("id", " ", 1, AttributeType.text())
        } shouldHaveMessage "Attribute name cannot be blank."

        shouldThrow<IllegalArgumentException> {
            Attribute.create("id", "invalid name!", 1, AttributeType.text())
        } shouldHaveMessage "Attribute name can only contain alphanumerics, hyphens, and underscores."
    }

    "Attribute는 중첩된 AttributeType을 포함하여 올바르게 (역)직렬화된다" {
        val attribute = Attribute.create(
            "id-2",
            "tags",
            2,
            AttributeType.array(AttributeType.text())
        )

        val json = objectMapper.writeValueAsString(attribute)
        val deserialized = objectMapper.readValue(json, Attribute::class.java)

        deserialized.name shouldBe attribute.name
        deserialized.type.simplify() shouldBe "text[]"
    }
})
