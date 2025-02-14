package dev.sayaya.handbook.domain

interface Attribute {
    val name: String
    val description: String?
    val type: AttributeType

    companion object {
        const val DEFAULT_NAME = ""
        val DEFAULT_DESCRIPTION: String? = null

        data class ValueAttribute (
            override val name: String = DEFAULT_NAME,
            override val description: String? = DEFAULT_DESCRIPTION
        ): Attribute {
            override val type: AttributeType = AttributeType.Value
        }
        data class ArrayAttribute (
            override val name: String = DEFAULT_NAME,
            override val description: String? = DEFAULT_DESCRIPTION,
            val valueType: AttributeType = AttributeType.Value
        ): Attribute {
            override val type: AttributeType = AttributeType.Array
        }
        data class MapAttribute (
            override val name: String = DEFAULT_NAME,
            override val description: String? = DEFAULT_DESCRIPTION,
            val keyType: AttributeType = AttributeType.Value,
            val valueType: AttributeType = AttributeType.Value
        ): Attribute {
            override val type: AttributeType = AttributeType.Map
        }
        data class DocumentAttribute (
            override val name: String = DEFAULT_NAME,
            override val description: String? = DEFAULT_DESCRIPTION,
            val referenceType: Type
        ): Attribute {
            override val type: AttributeType = AttributeType.Document
        }
        data class FileAttribute (
            override val name: String = DEFAULT_NAME,
            override val description: String? = DEFAULT_DESCRIPTION,
            val extensions: Set<String> = emptySet()
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