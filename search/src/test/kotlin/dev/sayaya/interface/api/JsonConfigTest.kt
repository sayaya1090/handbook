package dev.sayaya.`interface`.api

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.string.shouldContain
import java.time.Instant
import java.time.LocalDateTime

internal class JsonConfigTest : ShouldSpec({
    context("ObjectMapper 설정 테스트") {
        val objectMapper: ObjectMapper = JsonConfig().objectMapper()
        should("필드 이름이 snake_case로 변환되어야 한다") {
            data class TestDto(val name: String, val createdAt: LocalDateTime)
            val testDto = TestDto("example", LocalDateTime.of(2025, 11, 1, 0, 0))
            val serialized = objectMapper.writeValueAsString(testDto)
            serialized shouldContain "\"created_at\""
        }
        should("Instant가 epoch time으로 직렬화되어야 한다") {
            data class TestDto(val name: String, val createdAt: Instant)
            val testDto = TestDto("example", LocalDateTime.of(2025, 11, 1, 0, 0).toInstant(java.time.ZoneOffset.UTC))
            val serialized = objectMapper.writeValueAsString(testDto)
            println(serialized)
            serialized shouldContain "\"created_at\":1761955200000"
        }
    }
})