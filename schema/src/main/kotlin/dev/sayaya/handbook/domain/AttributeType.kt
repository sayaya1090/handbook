package dev.sayaya.handbook.domain

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.io.Serializable
import java.time.Instant

/**
 * 'Type'의 속성이 가질 수 있는 다양한 데이터 타입을 표현하는 타입 계층 구조.
 *
 * `sealed interface`를 사용하여 각 속성 타입이 자신만의 고유한 데이터와 유효성 검증 규칙을
 * 가질 수 있도록 모델링합니다. 이는 컴파일 시점의 타입 안전성을 보장하고,
 * 불필요한 nullable 프로퍼티를 제거하여 매우 견고하고 표현력 있는 모델을 만듭니다.
 *
 * Jackson 어노테이션을 통해 이 다형적 구조를 JSON으로 손쉽게 직렬화/역직렬화할 수 있습니다.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = AttributeType.Text::class, name = "text"),
    JsonSubTypes.Type(value = AttributeType.Bool::class, name = "bool"),
    JsonSubTypes.Type(value = AttributeType.Number::class, name = "number"),
    JsonSubTypes.Type(value = AttributeType.Date::class, name = "date"),
    JsonSubTypes.Type(value = AttributeType.Enum::class, name = "enum"),
    JsonSubTypes.Type(value = AttributeType.Array::class, name = "array"),
    JsonSubTypes.Type(value = AttributeType.Map::class, name = "map"),
    JsonSubTypes.Type(value = AttributeType.File::class, name = "file"),
    JsonSubTypes.Type(value = AttributeType.Document::class, name = "document")
)
sealed interface AttributeType : Serializable {
    /** 일반 텍스트. 정규식 패턴으로 유효성을 검증할 수 있습니다. */
    data class Text(val regexPatterns: List<String> = emptyList()) : AttributeType

    /** Boolean 값. 체크박스 등으로 표현됩니다. */
    data object Bool : AttributeType

    /** 숫자. 최솟값, 최댓값으로 범위를 제한할 수 있습니다. */
    data class Number(val min: Long? = null, val max: Long? = null) : AttributeType

    /** 날짜. 시작, 종료일로 범위를 제한할 수 있습니다. */
    data class Date(val after: Instant? = null, val before: Instant? = null) : AttributeType

    /** 미리 정의된 목록 중 하나를 선택하는 값. 셀렉트박스 등으로 표현됩니다. */
    data class Enum(val allowedValues: Set<String>) : AttributeType

    /** 여러 개의 값을 가지는 배열. 배열의 각 요소는 `elementType`을 따릅니다. */
    data class Array(val elementType: AttributeType) : AttributeType

    /** Key-Value 형태의 맵. 각 Key와 Value는 자신만의 `AttributeType`을 가집니다. */
    data class Map(val keyType: AttributeType, val valueType: AttributeType) : AttributeType

    /** 파일. 허용되는 확장자로 유효성을 검증합니다. */
    data class File(val extensions: Set<String>) : AttributeType {
        init {
            require(extensions.all { it.matches(Regex("^[a-zA-Z0-9]+$")) }) {
                "FileAttribute extensions must contain only alphanumeric characters."
            }
        }
    }

    /** 다른 'Type'을 참조하는 값. 참조되는 타입의 ID로 유효성을 검증합니다. */
    data class Document(val referencedType: String) : AttributeType
}