package dev.sayaya.handbook.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import dev.sayaya.handbook.entity.validator.ValidatorDefinition
import java.io.Serializable

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class AttributeTypeDefinition (
    val baseType: AttributeType, // "Value", "Array", "Map", "File", "Document" 등 (AttributeType enum 값 활용 가능)
    val typeArguments: List<AttributeTypeDefinition>? = null, // 예: Map의 경우 [keyDefinition, valueDefinition]
    val referencedType: String? = null, // 다른 Type 엔티티나 스키마를 참조하는 경우 ID
    val validators: List<ValidatorDefinition> = emptyList()
) : Serializable