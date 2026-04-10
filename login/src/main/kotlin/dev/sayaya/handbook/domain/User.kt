package dev.sayaya.handbook.domain

import java.time.LocalDateTime
import java.util.*

data class User(
    val id: UUID,
    val provider: String,
    val account: String,
    val name: String,
    val roles: MutableList<Role> = mutableListOf(),
    val lastLoginDateTime: LocalDateTime? = null,
) {
    fun toToken(nbf: LocalDateTime, exp: LocalDateTime, iss: String, iat: LocalDateTime) = Token(
        nbf = nbf,
        exp = exp,
        iss = iss,
        iat = iat,
        authorities = roles.map { it.name },
        name = name,
        id = id.toString(),
    )
}
