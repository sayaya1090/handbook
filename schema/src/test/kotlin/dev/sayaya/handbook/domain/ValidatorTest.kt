package dev.sayaya.handbook.domain

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import java.time.Instant
import java.time.temporal.ChronoUnit

class ValidatorTest : DescribeSpec({
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

    describe("Validator.Regex는") {
        val validator = Validator.Regex("^[a-z]{3}$")
        context("유효성 검증 로직이") {
            it("패턴에 맞는 문자열을 통과시킨다") { validator.validate("abc") shouldBe true }
            it("패턴에 맞지 않는 문자열을 거부한다") { validator.validate("abcd") shouldBe false }
            it("문자열이 아닌 타입을 거부한다") { validator.validate(123) shouldBe false }
        }
        context("생성 및 (역)직렬화가") {
            it("정상적으로 동작한다") {
                printJson("Validator.Regex", validator)
                val json = objectMapper.writeValueAsString(validator)
                json shouldBe """{"type":"regex","pattern":"^[a-z]{3}$"}"""
                objectMapper.readValue(json, Validator::class.java) shouldBe validator
            }
            it("빈 패턴으로 생성 시 예외를 발생시킨다") {
                shouldThrow<IllegalArgumentException> { Validator.Regex("") }
            }
        }
    }

    describe("Validator.Bool는") {
        val validator = Validator.Bool
        context("유효성 검증 로직이") {
            it("Boolean 타입을 통과시킨다") {
                validator.validate(true) shouldBe true
                validator.validate(false) shouldBe true
            }
            it("Boolean이 아닌 타입을 거부한다") {
                validator.validate("true") shouldBe false
                validator.validate(1) shouldBe false
            }
        }
        context("(역)직렬화가") {
            it("정상적으로 동작한다") {
                printJson("Validator.Bool", validator)
                val json = objectMapper.writeValueAsString(validator)
                json shouldBe """{"type":"bool"}"""
                objectMapper.readValue(json, Validator::class.java) shouldBe validator
            }
        }
    }

    describe("Validator.Number는") {
        val validator = Validator.Number(min = 0.0, max = 100.0)
        context("닫힌 구간에서 유효성 검증 로직이") {
            it("범위 안의 숫자를 통과시킨다") {
                validator.validate(50) shouldBe true
                validator.validate(0.0) shouldBe true
                validator.validate("50.5") shouldBe true
            }
            it("범위 밖의 숫자를 거부한다") {
                validator.validate(-1) shouldBe false
                validator.validate(101.0) shouldBe false
            }
            it("숫자가 아닌 타입을 거부한다") {
                validator.validate("not-a-number") shouldBe false
                validator.validate(true) shouldBe false
            }
        }
        context("최소값만 검증하는 경우 유효성 검증 로직이") {
            val atLeast10 = Validator.Number(min = 10.0)
            it("범위 안의 숫자를 통과시킨다") { atLeast10.validate(10) shouldBe true }
            it("범위 밖의 숫자를 거부한다") { atLeast10.validate(9.99) shouldBe false }
        }
        context("최대값만 검증하는 경우 유효성 검증 로직이") {
            val atMost20 = Validator.Number(max = 20.0)
            it("범위 안의 숫자를 통과시킨다") { atMost20.validate(20) shouldBe true }
            it("범위 밖의 숫자를 거부한다") { atMost20.validate(20.01) shouldBe false }
        }
        context("두 기준이 모두 null이면 예외를 발생시킨다") {
            it("Validator.Number(min = null, max = null) 는 IllegalArgumentException") {
                shouldThrow<IllegalArgumentException> {
                    Validator.Number()
                } shouldHaveMessage "ValidatorNumber: 'min' or 'max' must be specified."
            }
        }
        context("생성 및 (역)직렬화가") {
            it("정상적으로 동작한다") {
                printJson("Validator.Number", validator)
                val json = objectMapper.writeValueAsString(validator)
                json shouldBe """{"type":"number","min":0.0,"max":100.0}"""
                objectMapper.readValue(json, Validator::class.java) shouldBe validator
            }
            it("min > max 이면 생성 시 예외를 발생시킨다") {
                shouldThrow<IllegalArgumentException> { Validator.Number(min = 100.0, max = 0.0) }
            }
        }
    }

    describe("Validator.Date는") {
        val now = Instant.now()
        val after = now.plus(1, ChronoUnit.DAYS)
        val before = now.minus(1, ChronoUnit.DAYS)
        val validator = Validator.Date(upperBound = after, lowerBound = before)

        context("닫힌 구간에서 유효성 검증 로직이") {
            it("범위 안의 날짜를 통과시킨다") {
                validator.validate(now) shouldBe true
                validator.validate(now.toString()) shouldBe true
            }
            it("범위 밖의 날짜를 거부한다") {
                validator.validate(after.plusSeconds(1)) shouldBe false
                validator.validate(before.minusSeconds(1)) shouldBe false
            }
            it("날짜가 아닌 타입을 거부한다") {
                validator.validate("invalid-date") shouldBe false
                validator.validate(12345L) shouldBe false
            }
        }
        context("upperBound만 지정하고 lowerBound는 null인 경우 유효성 검증 로직이") {
            val upperBoundOnly = Validator.Date(upperBound = now)
            it("upperBound 이전(같음 포함)은 true") {
                upperBoundOnly.validate(before) shouldBe true
                upperBoundOnly.validate(now) shouldBe true
            }
            it("upperBound 이후는 false") { upperBoundOnly.validate(after) shouldBe false }
        }
        context("lowerBound만 지정하고 upperBound는 null인 경우 유효성 검증 로직이") {
            val lowerBoundOnly = Validator.Date(lowerBound = now)
            it("lowerBound 이후(같음 포함)는 true") {
                lowerBoundOnly.validate(after) shouldBe true
                lowerBoundOnly.validate(now) shouldBe true
            }
            it("lowerBound 이전은 false") { lowerBoundOnly.validate(before) shouldBe false }
        }
        context("두 기준이 모두 null이면 예외를 발생시킨다") {
            it("Validator.Date(lowerBound = null, upperBound = null) 는 IllegalArgumentException") {
                shouldThrow<IllegalArgumentException> {
                    Validator.Date()
                } shouldHaveMessage "ValidatorDate: 'lowerBound' or 'upperBound' must be specified."
            }
        }
        context("생성 및 (역)직렬화가") {
            it("정상적으로 동작한다") {
                printJson("Validator.Date", validator)
                val json = objectMapper.writeValueAsString(validator)
                json shouldBe """{"type":"date","lower_bound":"$before","upper_bound":"$after"}"""
                objectMapper.readValue(json, Validator::class.java) shouldBe validator
            }
            it("lowerBound가 upperBound보다 빠르면 생성 시 예외를 발생시킨다") {
                shouldThrow<IllegalArgumentException> { Validator.Date(upperBound = before, lowerBound = after) }
            }
        }
    }

    describe("Validator.Enum은") {
        val validator = Validator.Enum("A", "B", "C")
        context("유효성 검증 로직이") {
            it("옵션에 포함된 문자열을 통과시킨다") {
                validator.validate("A") shouldBe true
                validator.validate("C") shouldBe true
            }
            it("옵션에 없는 문자열을 거부한다") { validator.validate("D") shouldBe false }
            it("문자열이 아닌 타입을 거부한다") { validator.validate(1) shouldBe false }
        }
        context("생성 및 (역)직렬화가") {
            it("정상적으로 동작한다") {
                printJson("Validator.Enumeration", validator)
                val json = objectMapper.writeValueAsString(validator)
                // Set은 순서를 보장하지 않으므로, 역직렬화 후 내용만 비교
                objectMapper.readValue(json, Validator::class.java) shouldBe validator
            }
        }
    }
})