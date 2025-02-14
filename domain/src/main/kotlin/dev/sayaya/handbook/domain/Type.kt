package dev.sayaya.handbook.domain

@JvmRecord
data class Type (
    val id: String,
    val parent: Type?,
    val description: String?,
    val attributes: List<Attribute>
) {
    init {
        require(id.isNotBlank()) { "Type id cannot be blank" }
        require(isCircularParent(parent).not()) { "Circular parent reference detected in Type $id" }
    }

    private fun isCircularParent(parent: Type?): Boolean = when (parent) {
        null -> false
        this -> true
        else -> isCircularParent(parent.parent)
    }.also {
        println("parent:" + parent)
        println("this:" + this)
        println(parent == this)
    }
}