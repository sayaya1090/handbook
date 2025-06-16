package dev.sayaya.handbook.domain

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.io.Serializable
import java.time.Instant
import java.time.format.DateTimeParseException

@JsonTypeInfo(use = NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = ValidatorDefinition.Companion.ValidatorRegex::class, name = "REGEX"),
    JsonSubTypes.Type(value = ValidatorDefinition.Companion.ValidatorBool::class, name = "BOOL"),
    JsonSubTypes.Type(value = ValidatorDefinition.Companion.ValidatorNumber::class, name = "NUMBER"),
    JsonSubTypes.Type(value = ValidatorDefinition.Companion.ValidatorDate::class, name = "DATE"),
    JsonSubTypes.Type(value = ValidatorDefinition.Companion.ValidatorEnum::class, name = "ENUM")
) @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
sealed interface ValidatorDefinition: Serializable {
    fun validate(value: Any?): Boolean {
        throw NotImplementedError("ValidatorDefinition.validate() must be implemented in subclasses")
    }
    companion object {
        enum class ValidatorType {
            REGEX,                  // 값이 정규표현식을 만족한다
            BOOL, NUMBER, DATE,     // 값이 해당 타입이다(포맷, 범위 지원)
            ENUM                    // 값이 해당 값 중 하나이다. 파일 속성의 경우, 확장자가 해당 값 중 하나이다
        }
        data class ValidatorRegex (
            val pattern: String
        ): ValidatorDefinition {
            init {
                require(pattern.isNotBlank()) { "Regex cannot be blank." }
            }
            private val type: ValidatorType = ValidatorType.REGEX
            override fun validate(value: Any?): Boolean = if (value is String) {
                value.matches(Regex(pattern))
            } else false
        }
        class ValidatorBool: ValidatorDefinition {
            private val type: ValidatorType = ValidatorType.BOOL
            override fun validate(value: Any?): Boolean {
                return value is Boolean
            }
        }
        data class ValidatorNumber (
            val min: Number? = null,
            val max: Number? = null
        ): ValidatorDefinition {
            init {
                if (min != null && max != null) {
                    require(min.toDouble() <= max.toDouble()) {
                        "ValidatorNumber: min must be less than or equal to max."
                    }
                }
            }
            private val type: ValidatorType = ValidatorType.NUMBER
            override fun validate(value: Any?): Boolean = when (value) {
                is Number -> {
                    (min == null || value.toDouble() >= min.toDouble()) &&
                    (max == null || value.toDouble() <= max.toDouble())
                } is String -> try {
                    validate(value.toDouble())
                } catch(_: NumberFormatException) {
                    false
                } else -> false
            }
        }
        data class ValidatorDate (
            val before: Instant? = null, // 이 날짜 이전이어야 함 (ISO 8601 형식의 날짜 문자열)
            val after: Instant? = null   // 이 날짜 이후여야 함 (ISO 8601 형식의 날짜 문자열)
        ): ValidatorDefinition {
            init {
                if (before != null && after != null) {
                    require(before.isAfter(after)) {
                        "ValidatorDate: 'before' must be after 'after'."
                    }
                }
            }
            private val type: ValidatorType = ValidatorType.DATE
            override fun validate(value: Any?): Boolean = when (value) {
                is Instant -> {
                    (before == null || value.isBefore(before)) &&
                    (after == null || value.isAfter(after))
                } is String -> try {
                    validate(Instant.parse(value))
                } catch(_: DateTimeParseException) {
                    false
                } else -> false
            }
        }
        data class ValidatorEnum (
            val options: List<String>
        ): ValidatorDefinition {
            constructor(vararg allowedValues: String): this(allowedValues.toList())
            private val type: ValidatorType = ValidatorType.ENUM
            override fun validate(value: Any?): Boolean = when (value) {
                is String -> value in options
                else -> false
            }
        }
    }
}