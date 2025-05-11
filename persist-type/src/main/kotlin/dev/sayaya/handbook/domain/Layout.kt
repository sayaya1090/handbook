package dev.sayaya.handbook.domain

import java.time.Instant
import java.util.*

data class Layout (
    val workspace: UUID,
    val id: UUID
) {
    lateinit var effectDateTime: Instant
    lateinit var expireDateTime: Instant

    fun isInitialized(): Boolean {
        return this::effectDateTime.isInitialized && this::expireDateTime.isInitialized
    }
}