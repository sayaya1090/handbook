package dev.sayaya.handbook.domain

@JvmRecord
data class TypeWithLayout (
    val type: Type,
    val x: UShort,
    val y: UShort,
    val width: UShort,
    val height: UShort,
) {
    init {
        require(width > 0u) { "너비는 0보다 커야 합니다. 현재 값: $width" }
        require(height > 0u) { "높이는 0보다 커야 합니다. 현재 값: $height" }
    }
}