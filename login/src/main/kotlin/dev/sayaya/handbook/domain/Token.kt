package dev.sayaya.handbook.domain

import java.time.LocalDateTime
import java.util.*

data class Token (
    val nbf: LocalDateTime,
    val exp: LocalDateTime,
    val iss: String,
    val iat: LocalDateTime,
    val authorities: List<String>,
    val name: String,
    val id: String = UUID.randomUUID().toString()
)