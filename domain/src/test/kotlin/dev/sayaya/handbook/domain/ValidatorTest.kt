package dev.sayaya.handbook.domain

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import java.time.Instant

class ValidatorTest : StringSpec({
    // ValidatorRegex Tests
    "ValidatorRegex: 유효한 패턴으로 초기화되어야 한다" {
        val validator = ValidatorDefinition.Companion.ValidatorRegex(pattern = "^[a-zA-Z0-9]+$")
        validator.validate("Valid123") shouldBe true
    }

    "ValidatorRegex: 빈 패턴으로 초기화 시 IllegalArgumentException을 발생시킨다" {
        val exception = shouldThrow<IllegalArgumentException> {
            ValidatorDefinition.Companion.ValidatorRegex(pattern = "")
        }
        exception shouldHaveMessage "Regex cannot be blank."
    }

    "ValidatorRegex: 공백만 있는 패턴으로 초기화 시 IllegalArgumentException을 발생시킨다" {
        val exception = shouldThrow<IllegalArgumentException> {
            ValidatorDefinition.Companion.ValidatorRegex(pattern = "   ")
        }
        exception shouldHaveMessage "Regex cannot be blank."
    }

    "ValidatorRegex: validate는 패턴과 일치하는 문자열에 true를 반환한다" {
        val validator = ValidatorDefinition.Companion.ValidatorRegex(pattern = "\\d{3}-\\d{3}")
        validator.validate("123-456") shouldBe true
    }

    "ValidatorRegex: validate는 패턴과 일치하지 않는 문자열에 false를 반환한다" {
        val validator = ValidatorDefinition.Companion.ValidatorRegex(pattern = "\\d{3}-\\d{3}")
        validator.validate("abc-def") shouldBe false
        validator.validate("123456") shouldBe false
    }

    "ValidatorRegex: validate는 문자열이 아닌 타입에 false를 반환한다" {
        val validator = ValidatorDefinition.Companion.ValidatorRegex(pattern = ".*")
        validator.validate(123) shouldBe false
        validator.validate(true) shouldBe false
        validator.validate(Instant.now()) shouldBe false
    }

    // ValidatorBool Tests
    "ValidatorBool: validate는 Boolean true에 true를 반환한다" {
        val validator = ValidatorDefinition.Companion.ValidatorBool()
        validator.validate(true) shouldBe true
    }

    "ValidatorBool: validate는 Boolean false에 true를 반환한다" {
        val validator = ValidatorDefinition.Companion.ValidatorBool()
        validator.validate(false) shouldBe true
    }

    "ValidatorBool: validate는 Boolean이 아닌 타입에 false를 반환한다" {
        val validator = ValidatorDefinition.Companion.ValidatorBool()
        validator.validate("true") shouldBe false
        validator.validate(1) shouldBe false
        validator.validate(0) shouldBe false
        validator.validate(Instant.now()) shouldBe false
    }

    // ValidatorNumber Tests
    "ValidatorNumber: min과 max가 null일 때 모든 숫자에 true를 반환한다" {
        val validator = ValidatorDefinition.Companion.ValidatorNumber(min = null, max = null)
        validator.validate(100) shouldBe true
        validator.validate(-100) shouldBe true
        validator.validate(0.0) shouldBe true
        validator.validate(123.456) shouldBe true
    }

    "ValidatorNumber: min만 설정된 경우, min 이상인 숫자에 true를 반환한다" {
        val validator = ValidatorDefinition.Companion.ValidatorNumber(min = 10, max = null)
        validator.validate(10) shouldBe true
        validator.validate(11) shouldBe true
        validator.validate(9.99) shouldBe false
        validator.validate(-5) shouldBe false
    }

    "ValidatorNumber: max만 설정된 경우, max 이하인 숫자에 true를 반환한다" {
        val validator = ValidatorDefinition.Companion.ValidatorNumber(min = null, max = 100)
        validator.validate(100) shouldBe true
        validator.validate(99.99) shouldBe true
        validator.validate(100.01) shouldBe false
    }

    "ValidatorNumber: min과 max가 설정된 경우, 범위 내 숫자에 true를 반환한다" {
        val validator = ValidatorDefinition.Companion.ValidatorNumber(min = 0, max = 10)
        validator.validate(0) shouldBe true
        validator.validate(5) shouldBe true
        validator.validate(10) shouldBe true
        validator.validate(-0.01) shouldBe false
        validator.validate(10.01) shouldBe false
    }

    "ValidatorNumber: min이 max보다 클 경우 IllegalArgumentException을 발생시킨다" {
        val exception = shouldThrow<IllegalArgumentException> {
            ValidatorDefinition.Companion.ValidatorNumber(min = 10, max = 0)
        }
        exception shouldHaveMessage "ValidatorNumber: min must be less than or equal to max."
    }

    "ValidatorNumber: min과 max가 같을 경우는 허용된다" {
        shouldNotThrowAny {
            ValidatorDefinition.Companion.ValidatorNumber(min = 10, max = 10)
        }
        val validator = ValidatorDefinition.Companion.ValidatorNumber(min = 10, max = 10)
        validator.validate(10) shouldBe true
        validator.validate(9) shouldBe false
        validator.validate(11) shouldBe false
    }

    "ValidatorNumber: validate는 숫자가 아닌 타입에 false를 반환한다" {
        val validator = ValidatorDefinition.Companion.ValidatorNumber(min = 0, max = 100)
        validator.validate("50") shouldBe false
        validator.validate(true) shouldBe false
        validator.validate(Instant.now()) shouldBe false
    }

    // ValidatorDate Tests
    "ValidatorDate: before와 after가 null일 때 모든 Instant에 true를 반환한다" {
        val validator = ValidatorDefinition.Companion.ValidatorDate(before = null, after = null)
        val now = Instant.now()
        validator.validate(now) shouldBe true
        validator.validate(Instant.MAX) shouldBe true
        validator.validate(Instant.MIN) shouldBe true
    }

    "ValidatorDate: after만 설정된 경우, 해당 시점 이후의 Instant에 true를 반환한다" {
        val now = Instant.now()
        val oneHourLater = now.plusSeconds(3600)
        val oneHourBefore = now.minusSeconds(3600)
        val validator = ValidatorDefinition.Companion.ValidatorDate(after = now, before = null)
        validator.validate(oneHourLater) shouldBe true // now 이후
        validator.validate(now) shouldBe false         // now와 같으면 false (isAfter)
        validator.validate(oneHourBefore) shouldBe false // now 이전
    }

    "ValidatorDate: before만 설정된 경우, 해당 시점 이전의 Instant에 true를 반환한다" {
        val now = Instant.now()
        val oneHourLater = now.plusSeconds(3600)
        val oneHourBefore = now.minusSeconds(3600)
        val validator = ValidatorDefinition.Companion.ValidatorDate(after = null, before = now)
        validator.validate(oneHourBefore) shouldBe true // now 이전
        validator.validate(now) shouldBe false          // now와 같으면 false (isBefore)
        validator.validate(oneHourLater) shouldBe false  // now 이후
    }

    "ValidatorDate: after와 before가 설정된 경우, 두 시점 사이의 Instant에 true를 반환한다" {
        val now = Instant.now()
        val oneHourLater = now.plusSeconds(3600)
        val oneHourBefore = now.minusSeconds(3600)
        val validator = ValidatorDefinition.Companion.ValidatorDate(after = oneHourBefore, before = oneHourLater)
        validator.validate(now) shouldBe true // oneHourBefore < now < oneHourLater
        validator.validate(oneHourBefore) shouldBe false
        validator.validate(oneHourLater) shouldBe false
    }

    "ValidatorDate: init 시 before가 after보다 이전(과거)이면 IllegalArgumentException을 발생시킨다" {
        val now = Instant.now()
        val oneHourLater = now.plusSeconds(3600)
        val oneHourBefore = now.minusSeconds(3600)
        val exception = shouldThrow<IllegalArgumentException> {
            ValidatorDefinition.Companion.ValidatorDate(after = oneHourLater, before = oneHourBefore)
        }
        exception shouldHaveMessage "ValidatorDate: 'before' must be after 'after'."
    }

    "ValidatorDate: init 시 before와 after가 같으면 IllegalArgumentException을 발생시킨다" {
        val specificTime = Instant.parse("2023-01-01T12:00:00Z")
        val exception = shouldThrow<IllegalArgumentException> {
            ValidatorDefinition.Companion.ValidatorDate(after = specificTime, before = specificTime)
        }
        exception shouldHaveMessage "ValidatorDate: 'before' must be after 'after'."
    }

    "ValidatorDate: init 시 before가 after보다 이후(미래)이면 정상적으로 생성된다" {
        val now = Instant.now()
        val oneHourLater = now.plusSeconds(3600)
        val oneHourBefore = now.minusSeconds(3600)
        shouldNotThrowAny {
            ValidatorDefinition.Companion.ValidatorDate(after = oneHourBefore, before = oneHourLater)
        }
    }

    "ValidatorDate: validate는 Instant가 아닌 타입에 false를 반환한다" {
        val now = Instant.now()
        val oneHourLater = now.plusSeconds(3600)
        val oneHourBefore = now.minusSeconds(3600)
        val validator = ValidatorDefinition.Companion.ValidatorDate(after = oneHourBefore, before = oneHourLater)
        validator.validate("2023-10-27T10:00:00Z") shouldBe false
        validator.validate(1234567890L) shouldBe false
        validator.validate(true) shouldBe false
    }

    // ValidatorEnum Tests
    "ValidatorEnum: vararg 생성자로도 생성 가능해야 한다" {
        val varargValidator = ValidatorDefinition.Companion.ValidatorEnum("RED", "GREEN", "BLUE")
        varargValidator.validate("RED") shouldBe true
        varargValidator.validate("YELLOW") shouldBe false
    }

    "ValidatorEnum: validate는 allowedValues에 포함된 문자열에 true를 반환한다" {
        val allowed = listOf("APPLE", "BANANA", "CHERRY")
        val validator = ValidatorDefinition.Companion.ValidatorEnum(allowedValues = allowed)
        validator.validate("APPLE") shouldBe true
        validator.validate("BANANA") shouldBe true
    }

    "ValidatorEnum: validate는 allowedValues에 포함되지 않은 문자열에 false를 반환한다" {
        val allowed = listOf("APPLE", "BANANA", "CHERRY")
        val validator = ValidatorDefinition.Companion.ValidatorEnum(allowedValues = allowed)
        validator.validate("GRAPE") shouldBe false
        validator.validate("apple") shouldBe false // 대소문자 구분
    }

    "ValidatorEnum: validate는 문자열이 아닌 타입에 false를 반환한다" {
        val allowed = listOf("APPLE", "BANANA", "CHERRY")
        val validator = ValidatorDefinition.Companion.ValidatorEnum(allowedValues = allowed)
        validator.validate(123) shouldBe false
        validator.validate(true) shouldBe false
        validator.validate(Instant.now()) shouldBe false
    }

    "ValidatorEnum: allowedValues가 비어있는 경우 모든 문자열에 false를 반환한다" {
        val emptyEnumValidator = ValidatorDefinition.Companion.ValidatorEnum(emptyList())
        emptyEnumValidator.validate("ANYTHING") shouldBe false
        emptyEnumValidator.validate("") shouldBe false
    }
    // 테스트 내에서 사용할 헬퍼 데이터 클래스
    data class FieldValidationRule(val fieldName: String, val validator: ValidatorDefinition)

    // 테스트 내에서 사용할 헬퍼 검증 실행 함수
    fun validateData(data: Map<String, Any?>, rules: List<FieldValidationRule>): Map<String, Boolean> {
        return rules.associate { rule ->
            val value = data[rule.fieldName]
            rule.fieldName to rule.validator.validate(value)
        }
    }

    "MapValidation: 모든 필드가 유효할 때 모든 검증 결과가 true여야 한다" {
        val rules = listOf(
            FieldValidationRule("name", ValidatorDefinition.Companion.ValidatorRegex("^[a-zA-Z]+$")),
            FieldValidationRule("age", ValidatorDefinition.Companion.ValidatorNumber(min = 18, max = 99)),
            FieldValidationRule("active", ValidatorDefinition.Companion.ValidatorBool()),
            FieldValidationRule("event_time", ValidatorDefinition.Companion.ValidatorDate(after = Instant.parse("2023-01-01T00:00:00Z"))),
            FieldValidationRule("role", ValidatorDefinition.Companion.ValidatorEnum("ADMIN", "USER", "GUEST"))
        )
        val data: Map<String, Any?> = mapOf(
            "name" to "Alice",
            "age" to 30,
            "active" to true,
            "event_time" to Instant.parse("2024-01-01T10:00:00Z"),
            "role" to "USER"
        )

        val results = validateData(data, rules)
        results shouldContainExactly mapOf(
            "name" to true,
            "age" to true,
            "active" to true,
            "event_time" to true,
            "role" to true
        )
    }

    "MapValidation: 일부 필드가 유효하지 않을 때 해당 검증 결과가 false여야 한다" {
        val rules = listOf(
            FieldValidationRule("name", ValidatorDefinition.Companion.ValidatorRegex("^[a-zA-Z]+$")), // Valid
            FieldValidationRule("age", ValidatorDefinition.Companion.ValidatorNumber(min = 18, max = 30)),  // Invalid (35 > 30)
            FieldValidationRule("status", ValidatorDefinition.Companion.ValidatorEnum("ACTIVE", "INACTIVE")) // Invalid ("PENDING")
        )
        val data: Map<String, Any?> = mapOf(
            "name" to "Bob",
            "age" to 35,
            "status" to "PENDING"
        )

        val results = validateData(data, rules)
        results shouldContainExactly mapOf(
            "name" to true,
            "age" to false,
            "status" to false
        )
    }

    "MapValidation: 검증 대상 필드가 Map에 존재하지 않으면 해당 검증은 false여야 한다" {
        val rules = listOf(
            FieldValidationRule("name", ValidatorDefinition.Companion.ValidatorRegex(".+")),
            FieldValidationRule("required_field", ValidatorDefinition.Companion.ValidatorNumber(min = 0, max = null))
        )
        val data: Map<String, Any?> = mapOf(
            "name" to "Charlie" // required_field is missing
        )

        val results = validateData(data, rules)
        results shouldContainExactly mapOf(
            "name" to true,
            "required_field" to false // ValidatorNumber(null) is false
        )
    }

    "MapValidation: 필드 값으로 null이 제공되면 해당 검증은 false여야 한다 (현재 Validator 구현 기준)" {
        val rules = listOf(
            FieldValidationRule("nullable_regex", ValidatorDefinition.Companion.ValidatorRegex(".*")),
            FieldValidationRule("nullable_number", ValidatorDefinition.Companion.ValidatorNumber(min = null, max = null)),
            FieldValidationRule("nullable_date", ValidatorDefinition.Companion.ValidatorDate(after = null, before = null)),
            FieldValidationRule("nullable_enum", ValidatorDefinition.Companion.ValidatorEnum("A", "B")),
            FieldValidationRule("nullable_bool", ValidatorDefinition.Companion.ValidatorBool())
        )
        val data: Map<String, Any?> = mapOf(
            "nullable_regex" to null,
            "nullable_number" to null,
            "nullable_date" to null,
            "nullable_enum" to null,
            "nullable_bool" to null
        )
        val results = validateData(data, rules)
        results shouldContainExactly mapOf(
            "nullable_regex" to false,
            "nullable_number" to false,
            "nullable_date" to false,
            "nullable_enum" to false,
            "nullable_bool" to false
        )
    }

    "MapValidation: 필드 값의 타입이 올바르지 않으면 해당 검증은 false여야 한다" {
        val rules = listOf(
            FieldValidationRule("age", ValidatorDefinition.Companion.ValidatorNumber(min = 18, max = 99)), // Expects Number, gets String
            FieldValidationRule("event_time", ValidatorDefinition.Companion.ValidatorDate(after = Instant.parse("2023-01-01T00:00:00Z"))) // Expects Instant, gets String
        )
        val data: Map<String, Any?> = mapOf(
            "age" to "30", // String instead of Number
            "event_time" to "2024-01-01T10:00:00Z" // String instead of Instant
        )
        val results = validateData(data, rules)
        results shouldContainExactly mapOf(
            "age" to false,
            "event_time" to false
        )
    }
})