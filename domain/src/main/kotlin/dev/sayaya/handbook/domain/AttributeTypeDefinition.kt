package dev.sayaya.handbook.domain

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.io.Serializable

@JsonTypeInfo(use = NAME, property = "base_type")
@JsonSubTypes(
    JsonSubTypes.Type(value = AttributeTypeDefinition.Companion.ValueType::class, name = "Value"),
    JsonSubTypes.Type(value = AttributeTypeDefinition.Companion.ArrayType::class, name = "Array"),
    JsonSubTypes.Type(value = AttributeTypeDefinition.Companion.MapType::class, name = "Map"),
    JsonSubTypes.Type(value = AttributeTypeDefinition.Companion.DocumentType::class, name = "Document"),
    JsonSubTypes.Type(value = AttributeTypeDefinition.Companion.FileType::class, name = "File")
) @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
sealed interface AttributeTypeDefinition: Serializable {
    companion object {
        enum class AttributeType {
            Value,  // File, Document가 아닌 1개 값(텍스트, 날짜, 숫자, ...)
            Array,  // x개 값
            Map,    // Key-Value 형태
            File,
            Document
        }
        data class ValueType (
            // Regex: 여러 개 추가 가능, Bool, Number, Date, Enum: 하나만 적용 가능
            val validators: List<ValidatorDefinition> = emptyList()
        ): AttributeTypeDefinition
        data class ArrayType (
            val type: AttributeTypeDefinition
        ): AttributeTypeDefinition {
            val arguments: List<AttributeTypeDefinition> = listOf(type)
        }
        data class MapType (
            val key: AttributeTypeDefinition,
            val value: AttributeTypeDefinition,
        ): AttributeTypeDefinition {
            val arguments: List<AttributeTypeDefinition> = listOf(key, value)
        }
        data class FileType (
            val extensions: Set<String>,
        ): AttributeTypeDefinition {
            init {
                require(extensions.all { it.matches(Regex("^[a-zA-Z0-9]+$")) }) {
                    "FileAttribute extensions must contain only alphanumeric characters."
                }
            }
        }
        data class DocumentType (
            val referencedType: String
        ): AttributeTypeDefinition
    }
}