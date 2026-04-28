package dev.sayaya.handbook.domain

import com.fasterxml.jackson.annotation.JsonInclude
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.module.kotlin.kotlinModule
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class AttributeTypeTest : StringSpec({

    val objectMapper: ObjectMapper = JsonMapper.builder()
        .addModule(kotlinModule())
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
        .changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_EMPTY) }
        .build()

    "Text 타입은 올바르게 직렬화되고 단순화된다" {
        val type = AttributeType.text()
        type.simplify() shouldBe "text"
        
        val json = objectMapper.writeValueAsString(type)
        val deserialized = objectMapper.readValue(json, AttributeType::class.java)
        deserialized.type shouldBe "text"
    }

    "Array 타입은 중첩된 요소를 포함하여 올바르게 단순화된다" {
        val type = AttributeType.array(AttributeType.text())
        type.simplify() shouldBe "text[]"
        
        val json = objectMapper.writeValueAsString(type)
        val deserialized = objectMapper.readValue(json, AttributeType::class.java)
        deserialized.elementType.type shouldBe "text"
    }

    "Document 타입은 참조 타입명을 반환한다" {
        val type = AttributeType()
        type.type = "document"
        type.referencedType = "Customer"
        type.simplify() shouldBe "Customer"
    }
})
