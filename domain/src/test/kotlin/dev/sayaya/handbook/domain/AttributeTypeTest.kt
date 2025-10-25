package dev.sayaya.handbook.domain

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import java.time.Instant

class AttributeTypeTest : StringSpec({
    val objectMapper: ObjectMapper = JsonMapper.builder()
        .addModule(JavaTimeModule())
        .addModule(kotlinModule())
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .serializationInclusion(JsonInclude.Include.NON_EMPTY) // NON_NULL에서 NON_EMPTY로 변경
        .build()

    /**
     * 테스트 중인 객체를 설명과 함께 예쁘게 포맷팅된 JSON으로 콘솔에 출력합니다.
     * API 명세 작성이나 디버깅 시에 사용하면 편리합니다.
     * @param obj 직렬화할 객체
     */
    fun printJson(obj: Any) {
        println("\n--- JSON for: ${ obj.javaClass.simpleName } ---")
        println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj))
    }

    "Text 타입을 올바르게 생성하고 (역)직렬화한다" {
        val text = AttributeType.Text(regexPatterns = listOf("^\\d+$"))
        val json = objectMapper.writeValueAsString(text)
        val deserialized = objectMapper.readValue(json, AttributeType::class.java)

        printJson(text)
        json shouldBe """{"type":"text","regex_patterns":["^\\d+$"]}"""
        deserialized shouldBe text
    }

    "Bool 타입을 올바르게 나타내고 (역)직렬화한다" {
        val bool = AttributeType.Bool
        val json = objectMapper.writeValueAsString(bool)
        val deserialized = objectMapper.readValue(json, AttributeType::class.java)

        printJson(bool)
        json shouldBe """{"type":"bool"}"""
        deserialized shouldBe bool
    }

    "Number 타입을 올바르게 생성하고 (역)직렬화한다" {
        val number = AttributeType.Number(min = 0, max = 100)
        val json = objectMapper.writeValueAsString(number)
        val deserialized = objectMapper.readValue(json, AttributeType::class.java)

        printJson(number)
        json shouldBe """{"type":"number","min":0,"max":100}"""
        deserialized shouldBe number
    }

    "Date 타입을 올바르게 생성하고 (역)직렬화한다" {
        val now = Instant.now()
        val date = AttributeType.Date(after = now)
        val json = objectMapper.writeValueAsString(date)
        val deserialized = objectMapper.readValue(json, AttributeType::class.java)

        printJson(date)
        json shouldBe """{"type":"date","after":"$now"}"""
        deserialized shouldBe date
    }

    "Enum 타입을 올바르게 생성하고 (역)직렬화한다" {
        val enumType = AttributeType.Enum(allowedValues = setOf("A", "B"))
        val json = objectMapper.writeValueAsString(enumType)
        val deserialized = objectMapper.readValue(json, AttributeType::class.java)

        printJson(enumType)
        json shouldBe """{"type":"enum","allowed_values":["A","B"]}"""
        deserialized shouldBe enumType
    }

    "Array 타입을 올바르게 생성하고 (역)직렬화한다" {
        val array = AttributeType.Array(elementType = AttributeType.Text())
        val json = objectMapper.writeValueAsString(array)
        val deserialized = objectMapper.readValue(json, AttributeType::class.java)

        printJson(array)
        json shouldBe """{"type":"array","element_type":{"type":"text"}}"""
        deserialized shouldBe array
    }

    "Map 타입을 올바르게 생성하고 (역)직렬화한다" {
        val map = AttributeType.Map(
            keyType = AttributeType.Text(),
            valueType = AttributeType.Number()
        )
        val json = objectMapper.writeValueAsString(map)
        val deserialized = objectMapper.readValue(json, AttributeType::class.java)

        printJson(map)
        json shouldBe """{"type":"map","key_type":{"type":"text"},"value_type":{"type":"number"}}"""
        deserialized shouldBe map
    }

    "File 타입을 올바르게 생성/검증하고 (역)직렬화한다" {
        val file = AttributeType.File(extensions = setOf("pdf", "jpg"))
        val json = objectMapper.writeValueAsString(file)
        val deserialized = objectMapper.readValue(json, AttributeType::class.java)

        printJson(file)
        json shouldBe """{"type":"file","extensions":["pdf","jpg"]}"""
        deserialized shouldBe file

        shouldThrow<IllegalArgumentException> {
            AttributeType.File(extensions = setOf("pdf", ".exe"))
        } shouldHaveMessage "FileAttribute extensions must contain only alphanumeric characters."
    }

    "Document 타입을 올바르게 생성하고 (역)직렬화한다" {
        val document = AttributeType.Document(referencedType = "Invoice")
        val json = objectMapper.writeValueAsString(document)
        val deserialized = objectMapper.readValue(json, AttributeType::class.java)

        printJson(document)
        json shouldBe """{"type":"document","referenced_type":"Invoice"}"""
        deserialized shouldBe document
    }
})