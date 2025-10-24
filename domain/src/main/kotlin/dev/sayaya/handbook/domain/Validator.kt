package dev.sayaya.handbook.domain

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.io.Serializable
import java.time.Instant
import java.time.format.DateTimeParseException

/**
 * 속성(Attribute)의 값을 검증하는 규칙을 정의하는 타입 계층 구조.
 *
 * `sealed interface`를 사용하여 각 유효성 검증 타입이 자신만의 고유한 로직과 데이터를 갖도록 모델링합니다.
 * Jackson 어노테이션을 통해 이 다형적 구조를 JSON으로 손쉽게 직렬화/역직렬화할 수 있습니다.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = Validator.Regex::class, name = "regex"),
    JsonSubTypes.Type(value = Validator.Bool::class, name = "bool"),
    JsonSubTypes.Type(value = Validator.Number::class, name = "number"),
    JsonSubTypes.Type(value = Validator.Date::class, name = "date"),
    JsonSubTypes.Type(value = Validator.Enum::class, name = "enum")
)
sealed interface Validator : Serializable {
    /**
     * 주어진 값이 유효성 검증 규칙을 통과하는지 확인합니다.
     * @param value 검증할 값
     * @return 유효하면 true
     */
    fun validate(value: Any?): Boolean

    /** 값이 정규표현식을 만족하는지 검증합니다. */
    data class Regex(val pattern: String) : Validator {
        init {
            require(pattern.isNotBlank()) { "Regex pattern cannot be blank." }
        }
        override fun validate(value: Any?): Boolean = value is String && value.matches(kotlin.text.Regex(pattern))
    }

    /** 값이 Boolean 타입인지 검증합니다. */
    data object Bool : Validator {
        override fun validate(value: Any?): Boolean = value is Boolean
    }

    /** 값이 숫자이며, 선택적으로 min/max 범위 안에 있는지 검증합니다. */
    data class Number(val min: Double? = null, val max: Double? = null) : Validator {
        init {
            require(!(min == null && max == null)) {
                "ValidatorNumber: 'min' or 'max' must be specified."
            }
            if (min != null && max != null) {
                require(min <= max) { "ValidatorNumber: min must be less than or equal to max." }
            }
        }
        override fun validate(value: Any?): Boolean = when (value) {
            is kotlin.Number -> {
                val doubleValue = value.toDouble()
                (min == null || doubleValue >= min) && (max == null || doubleValue <= max)
            }
            is String -> try {
                validate(value.toDouble())
            } catch (_: NumberFormatException) {
                false
            }
            else -> false
        }
    }

    /** 값이 날짜(ISO 8601)이며, 선택적으로 lowerBound/upperBound 범위 안에 있는지 검증합니다. */
    data class Date(val lowerBound: Instant? = null, val upperBound: Instant? = null) : Validator {
        init {
            require(!(lowerBound == null && upperBound == null)) {
                "ValidatorDate: 'lowerBound' or 'upperBound' must be specified."
            }
            if (lowerBound != null && upperBound != null) {
                require(upperBound.isAfter(lowerBound)) { "ValidatorDate: 'upperBound' must be after 'lowerBound'." }
            }
        }
        override fun validate(value: Any?): Boolean = when (value) {
            is Instant -> {
                (lowerBound == null || value == lowerBound || value.isAfter(lowerBound))
             && (upperBound == null  || value == upperBound  || value.isBefore(upperBound))
            }
            is String -> try {
                validate(Instant.parse(value))
            } catch (_: DateTimeParseException) {
                false
            }
            else -> false
        }
    }

    /** 값이 주어진 목록 중 하나인지 검증합니다. */
    data class Enum(val options: Set<String>) : Validator {
        constructor(vararg allowedValues: String) : this(allowedValues.toSet())
        override fun validate(value: Any?): Boolean = value is String && value in options
    }
}