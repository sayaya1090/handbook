package dev.sayaya.handbook.domain

import java.time.LocalDateTime
import java.util.*

data class User (
    val id: UUID,
    val provider: String,
    val account: String,
    val name: String,
    val roles: MutableList<Role> = mutableListOf()
) {
    lateinit var lastLoginDateTime: LocalDateTime
    fun toToken (
        nbf: LocalDateTime,
        exp: LocalDateTime,
        iss: String,
        iat: LocalDateTime,
    ): Token = Token(nbf, exp, iss, iat, roles.map { "ROLE_$it" }, id.toString())
}