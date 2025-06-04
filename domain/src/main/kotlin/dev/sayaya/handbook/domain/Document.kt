package dev.sayaya.handbook.domain

import java.io.Serializable
import java.time.Instant
import java.util.*

// CreateDateTime이 null이면 새로 만들어진 객체, id=null
// 그렇지 않으면, DB에 반영된 객체
@JvmRecord
data class Document (
    val id: UUID?,
    val type: String,
    val serial: String,
    val effectDateTime: Instant,
    val expireDateTime: Instant,
    val createDateTime: Instant?,
    val creator: String?,
    val data: Map<String, String?>,
    val validations: Validation?
): Serializable {
    init {
        require(serial.matches(Regex("^[a-zA-Z0-9-_]+$"))) { "Document serial must be alphanumeric and may include hyphens and underscores." }
        require(expireDateTime.isAfter(effectDateTime)) { "Expire date time must be after effect date time" }
        require(id == null || (createDateTime != null && creator != null)) { "If id is not null, createDateTime and creator must be not null" }
    }
}