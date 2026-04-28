package dev.sayaya.handbook.interfaces.api

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import java.time.Instant
import java.util.*

class DocumentControllerHelperTest : DescribeSpec({
    val controller = DocumentController(mockk())
    val method = DocumentController::class.java.getDeclaredMethod("toInstant", String::class.java)
    method.isAccessible = true

    describe("DocumentController.toInstant") {
        it("null이나 빈 문자열은 null을 반환한다") {
            method.invoke(controller, null) shouldBe null
            method.invoke(controller, "") shouldBe null
            method.invoke(controller, "  ") shouldBe null
        }
        it("ISO-8601 DateTime 형식을 파싱한다") {
            val date = "2023-01-01T10:00:00Z"
            method.invoke(controller, date) shouldBe Instant.parse(date)
        }
        it("ISO-8601 Date 형식을 파싱한다") {
            val date = "2023-01-01"
            method.invoke(controller, date) shouldBe Instant.parse("2023-01-01T00:00:00Z")
        }
        it("yyyyMMdd 형식을 파싱한다") {
            val date = "20230101"
            method.invoke(controller, date) shouldBe Instant.parse("2023-01-01T00:00:00Z")
        }
        it("yyyy.MM.dd 형식을 파싱한다") {
            val date = "2023.01.01"
            method.invoke(controller, date) shouldBe Instant.parse("2023-01-01T00:00:00Z")
        }
        it("yyyy-MM-dd HH:mm:ss 형식을 파싱한다") {
            val date = "2023-01-01 10:00:00"
            method.invoke(controller, date) shouldBe Instant.parse("2023-01-01T10:00:00Z")
        }
        it("잘못된 형식은 IllegalArgumentException을 던진다") {
            // Reflection wraps exception in InvocationTargetException
            val ex = shouldThrow<java.lang.reflect.InvocationTargetException> {
                method.invoke(controller, "invalid")
            }
            ex.targetException.javaClass shouldBe IllegalArgumentException::class.java
        }
    }
    describe("DocumentController.fullTextSearch Validation") {
        it("쿼리 길이가 최대치를 초과하면 400 에러를 반환한다") {
            val longQuery = "a".repeat(DocumentController.MAX_QUERY_LENGTH + 1)
            reactor.test.StepVerifier.create(controller.fullTextSearch(UUID.randomUUID(), longQuery, 0, 50))
                .expectError(org.springframework.web.server.ResponseStatusException::class.java)
                .verify()
        }
    }
    describe("DocumentController.PARSERS") {
        it("모든 파서가 등록되어 있다") {
            DocumentController.PARSERS.size shouldBe 5
        }
    }
})
