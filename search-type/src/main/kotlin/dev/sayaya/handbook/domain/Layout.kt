package dev.sayaya.handbook.domain

import java.time.Instant
import java.util.*

@JvmRecord
data class Layout (
    val workspace: UUID,
    val effectDateTime: Instant,
    val expireDateTime: Instant
) {
    init {
        require(expireDateTime.isAfter(effectDateTime)) { "Expire date time must be after effect date time" }
    }
}