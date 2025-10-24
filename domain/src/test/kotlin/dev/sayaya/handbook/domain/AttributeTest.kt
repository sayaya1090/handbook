package dev.sayaya.handbook.domain

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.throwable.shouldHaveMessage

class AttributeTest : StringSpec({

    val objectMapper: ObjectMapper = JsonMapper.builder()
        .addModule(kotlinModule())
        .addModule(com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .serializationInclusion(JsonInclude.Include.NON_EMPTY)
        .build()

    fun printJson(description: String, obj: Any) {
        println("\n--- JSON for: $description ---")
        println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj))
    }

    "Attribute는 올바른 값이 주어지면 성공적으로 생성된다" {
        val attribute = Attribute(
            name = "author",
            order = 1,
            description = "작성자",
            type = AttributeType.Text(),
            nullable = false,
            inherited = false
        )
        attribute.name shouldBe "author"
        attribute.type shouldBe AttributeType.Text()
    }

    "Attribute는 유효하지 않은 이름으로 생성 시 예외를 발생시킨다" {
        shouldThrow<IllegalArgumentException> {
            Attribute(" ", 1, null, AttributeType.Text(), false, false)
        } shouldHaveMessage "Attribute name cannot be blank."

        shouldThrow<IllegalArgumentException> {
            Attribute("invalid name!", 1, null, AttributeType.Text(), false, false)
        } shouldHaveMessage "Attribute name can only contain alphanumerics, hyphens, and underscores."
    }

    "Attribute는 값 객체로서 모든 프로퍼티가 같을 때만 동일하다" {
        val baseAttr = Attribute("author", 1, "desc", AttributeType.Text(), false, false)
        val sameAttr = Attribute("author", 1, "desc", AttributeType.Text(), false, false)
        val differentName = baseAttr.copy(name = "editor")
        val differentType = baseAttr.copy(type = AttributeType.Number())

        baseAttr shouldBe sameAttr
        baseAttr.hashCode() shouldBe sameAttr.hashCode()
        baseAttr shouldNotBe differentName
    }

    "Attribute는 중첩된 AttributeType을 포함하여 올바르게 (역)직렬화된다" {
        val attribute = Attribute(
            name = "tags",
            order = 2,
            description = "문서 태그 목록",
            type = AttributeType.Array(
                elementType = AttributeType.Text(regexPatterns = listOf("^[a-z]+$"))
            ),
            nullable = true,
            inherited = false
        )

        printJson("Attribute with nested AttributeType", attribute)
        val json = objectMapper.writeValueAsString(attribute)
        val deserialized = objectMapper.readValue(json, Attribute::class.java)

        // 중첩된 객체를 포함한 전체 구조가 snake_case로 올바르게 변환되었는지,
        // 그리고 다시 원래 객체로 완벽하게 복원되는지 확인
        deserialized shouldBe attribute
    }
})