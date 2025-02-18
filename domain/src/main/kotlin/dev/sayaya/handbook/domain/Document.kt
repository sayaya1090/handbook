package dev.sayaya.handbook.domain

import java.util.*

@JvmRecord
data class Document (
    val id: UUID,
    val serial: String
)