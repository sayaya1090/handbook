package dev.sayaya.handbook.domain

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME


@JsonTypeInfo(use = NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = Attribute.Companion.ValueAttribute::class, name = "Value"),
    JsonSubTypes.Type(value = Attribute.Companion.ArrayAttribute::class, name = "Array"),
    JsonSubTypes.Type(value = Attribute.Companion.MapAttribute::class, name = "Map"),
    JsonSubTypes.Type(value = Attribute.Companion.DocumentAttribute::class, name = "Document"),
    JsonSubTypes.Type(value = Attribute.Companion.FileAttribute::class, name = "File")
)
interface Attribute {
    val name: String
    val order: Short
    val description: String?
    val type: AttributeType
    val nullable: Boolean
    val inherited: Boolean

    companion object {
        const val DEFAULT_NAME = ""
        val DEFAULT_DESCRIPTION: String? = null
        interface HasKeyType {
            val keyType: AttributeType
        }
        interface HasValueType {
            val valueType: AttributeType
        }
        data class ValueAttribute (
            override val name: String = DEFAULT_NAME,
            override val order: Short = 0,
            override val description: String? = DEFAULT_DESCRIPTION,
            override val nullable: Boolean = false,
            override val inherited: Boolean
        ): Attribute {
            override val type: AttributeType = AttributeType.Value
        }
        data class ArrayAttribute (
            override val name: String = DEFAULT_NAME,
            override val order: Short = 0,
            override val description: String? = DEFAULT_DESCRIPTION,
            override val valueType: AttributeType = AttributeType.Value,
            override val nullable: Boolean = false,
            override val inherited: Boolean
        ): Attribute, HasValueType {
            override val type: AttributeType = AttributeType.Array
        }
        data class MapAttribute (
            override val name: String = DEFAULT_NAME,
            override val order: Short = 0,
            override val description: String? = DEFAULT_DESCRIPTION,
            override val keyType: AttributeType = AttributeType.Value,
            override val valueType: AttributeType = AttributeType.Value,
            override val nullable: Boolean = false,
            override val inherited: Boolean
        ): Attribute, HasKeyType, HasValueType {
            override val type: AttributeType = AttributeType.Map
        }
        data class DocumentAttribute (
            override val name: String = DEFAULT_NAME,
            override val order: Short = 0,
            override val description: String? = DEFAULT_DESCRIPTION,
            val referenceType: String,
            override val nullable: Boolean = false,
            override val inherited: Boolean
        ): Attribute {
            override val type: AttributeType = AttributeType.Document
        }
        data class FileAttribute (
            override val name: String = DEFAULT_NAME,
            override val order: Short = 0,
            override val description: String? = DEFAULT_DESCRIPTION,
            val extensions: Set<String> = emptySet(),
            override val nullable: Boolean = false,
            override val inherited: Boolean
        ): Attribute {
            override val type: AttributeType = AttributeType.File
            init {
                require(extensions.all { it.matches(Regex("^[a-zA-Z0-9]+$")) }) {
                    "FileAttribute extensions must contain only alphanumeric characters."
                }
            }
        }
    }
}