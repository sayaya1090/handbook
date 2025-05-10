package dev.sayaya.handbook.domain

data class Attribute (
    val name: String,
    val order: Short,
    val description: String?,
    val type: AttributeTypeDefinition,
    val nullable: Boolean,
    val inherited: Boolean
)