package dev.sayaya.handbook.entity.validator

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.io.Serializable

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type" // JSON에서 이 프로퍼티 값을 기준으로 어떤 하위 타입으로 역직렬화할지 결정합니다.
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ValidatorRegex::class, name = "REGEX"),
    JsonSubTypes.Type(value = ValidatorBool::class, name = "BOOL"),
    JsonSubTypes.Type(value = ValidatorNumber::class, name = "NUMBER"),
    JsonSubTypes.Type(value = ValidatorDate::class, name = "DATE"),
    JsonSubTypes.Type(value = ValidatorEnum::class, name = "ENUM")
)
sealed interface ValidatorDefinition: Serializable {
    val type: ValidatorType
}