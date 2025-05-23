package dev.sayaya.handbook.`interface`.api

import com.github.f4b6a3.ulid.Ulid
import dev.sayaya.handbook.domain.Document
import java.time.Instant
import java.util.UUID

@JvmRecord
data class DocumentParam (
    private val id: String,

    private val type: String,
    private val effectDateTime: Instant,
    private val expireDateTime: Instant,
    private val data: Map<String, String?>,

    val document: Document = Document (
        id = if(id.isUuid()) UUID.fromString(id) else Ulid.fast().toUuid(),
        serial = "dd",
        type = type,
        effectDateTime = effectDateTime,
        expireDateTime = expireDateTime,
        data = data
    ),
    val delete: Boolean
) {
    companion object {
        fun String?.isUuid(): Boolean {
            if (this == null) return false
            return runCatching {
                UUID.fromString(this)
            }.isSuccess
        }
    }
}