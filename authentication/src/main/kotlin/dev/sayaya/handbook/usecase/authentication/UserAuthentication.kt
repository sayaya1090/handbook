package dev.sayaya.handbook.usecase.authentication

import io.jsonwebtoken.Claims
import org.springframework.security.authentication.AbstractAuthenticationToken
import java.time.LocalDateTime
import java.time.ZoneId

data class UserAuthentication (
    val id: String?,
    val username: String,ㅈ
    val issuer: String,
    val audience: Set<String>,
    val issuedDateTime: LocalDateTime,
    val notBeforeDateTime: LocalDateTime,
    val expireDateTime: LocalDateTime,
    private val token: String
): AbstractAuthenticationToken(emptySet()) {
    constructor(claims: Claims, token: String): this (
        id = claims.id,
        username = claims.get("name", String::class.java),
        issuer = claims.issuer,
        audience = claims.audience,
        issuedDateTime = claims.issuedAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
        notBeforeDateTime = claims.notBefore.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
        expireDateTime = claims.expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
        token = token
    )
    override fun getName(): String = username
    override fun getCredentials(): String = token
    override fun getPrincipal(): String = username
}