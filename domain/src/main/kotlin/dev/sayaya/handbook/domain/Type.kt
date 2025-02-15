package dev.sayaya.handbook.domain

@JvmRecord
data class Type (
    val id: String,
    val parent: String?,
    val description: String?,
    val primitive: Boolean,
    val attributes: List<Attribute>
) {
    init {
        require(id.isNotBlank()) { "Type id cannot be blank" }
    }
}