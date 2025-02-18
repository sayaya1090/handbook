package dev.sayaya.handbook.domain

import java.time.Instant

@JvmRecord
data class Type (
    val id: String,
    val version: String,
    val effectDateTime: Instant,
    val expireDateTime: Instant,
    val description: String?,
    val primitive: Boolean,
    val attributes: List<Attribute>,
    val parent: String? = null,
) {
    init {
        require(id.isNotBlank()) { "Type id cannot be blank" }
    }
}