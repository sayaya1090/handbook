package dev.sayaya.handbook.domain

import java.time.Instant
import java.util.*

@JvmRecord
data class Document (
    val id: UUID,
    val type: String,
    val serial: String,
    val effectDateTime: Instant,
    val expireDateTime: Instant,
    val data: Map<String, String?>
){
    init {
        require(serial.matches(Regex("^[a-zA-Z0-9-_]+$"))) { "Document serial must be alphanumeric and may include hyphens and underscores." }
        require(expireDateTime.isAfter(effectDateTime)) { "Expire date time must be after effect date time" }
    }
}