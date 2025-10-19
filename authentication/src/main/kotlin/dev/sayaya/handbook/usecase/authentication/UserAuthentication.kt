package dev.sayaya.handbook.usecase.authentication

import org.springframework.security.authentication.AbstractAuthenticationToken
import java.time.LocalDateTime

/**
 * 사용자 인증 정보를 담는 Authentication 구현체
 *
 * JWT 토큰에서 추출한 사용자 정보를 Spring Security의 인증 객체로 표현합니다.
 *
 * @property id 사용자 고유 식별자 (JWT의 jti 클레임)
 * @property username 사용자 이름 (JWT의 name 클레임)
 * @property issuer 토큰 발급자 (JWT의 iss 클레임)
 * @property issuedDateTime 토큰 발급 일시 (JWT의 iat 클레임)
 * @property notBeforeDateTime 토큰 유효 시작 일시 (JWT의 nbf 클레임)
 * @property expireDateTime 토큰 만료 일시 (JWT의 exp 클레임)
 * @property token 원본 JWT 토큰 문자열
 */
data class UserAuthentication (
    val id: String?,
    val username: String,
    val issuer: String,
    val issuedDateTime: LocalDateTime,
    val notBeforeDateTime: LocalDateTime,
    val expireDateTime: LocalDateTime,
    private val token: String
): AbstractAuthenticationToken(emptySet()) {
    override fun getName(): String = username
    override fun getCredentials(): String = token
    override fun getPrincipal(): String = username
    override fun setAuthenticated(isAuthenticated: Boolean) {
        if (this.isAuthenticated && !isAuthenticated) {
            throw IllegalArgumentException("Cannot set this token to unauthenticated; it is already authenticated.")
        }
        super.setAuthenticated(isAuthenticated)
    }
}