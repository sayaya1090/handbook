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
    val attributes: List<Attribute> = emptyList(),
    val parent: String? = null,

    val x: UShort,
    val y: UShort,
    val width: UShort,
    val height: UShort,
    val prev: String? = null,
    val next: String? = null,
) {
    init {
        require(id.isNotBlank()) { "Type id cannot be blank" }
        require(id.matches(Regex("^[a-zA-Z0-9가-힣_-]+$"))) { "Type id can only contain alphabet, 한글, numbers, hyphens, and underscores." }
        require(expireDateTime.isAfter(effectDateTime)) { "Expire date time must be after effect date time" }
        require(width > 0u) { "Width must be greater than 0. Current value: $width" }
        require(height > 0u) { "Height must be greater than 0. Current value: $height" }
    }
}
