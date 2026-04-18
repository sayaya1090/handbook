package dev.sayaya.handbook.interfaces.authentication

import org.springframework.security.authentication.AbstractAuthenticationToken
import java.time.LocalDateTime

/**
 * JWT 토큰에서 추출한 사용자 정보를 Spring Security의 인증 객체로 표현하는 어댑터.
 *
 * Spring Security의 [AbstractAuthenticationToken]을 상속하므로 interfaces 계층에 위치한다.
 *
 * ### Principal 계약
 * [getPrincipal]은 `this` 를 반환한다. Spring Security 의 `@AuthenticationPrincipal`
 * 은 `Authentication.getPrincipal()` 의 반환값을 컨트롤러 파라미터에 주입하므로,
 * 컨트롤러에서 `@AuthenticationPrincipal UserAuthentication` 으로 받으려면 principal
 * 자체가 `UserAuthentication` 이어야 한다. 이전에는 `username` (String) 을 반환해
 * 타입 불일치로 null 이 주입되던 회귀가 있었다.
 *
 * @property id 사용자 고유 식별자 (JWT의 jti 클레임)
 * @property username 사용자 이름 (JWT의 name 클레임)
 * @property issuer 토큰 발급자 (JWT의 iss 클레임)
 * @property issuedDateTime 토큰 발급 일시 (JWT의 iat 클레임)
 * @property notBeforeDateTime 토큰 유효 시작 일시 (JWT의 nbf 클레임)
 * @property expireDateTime 토큰 만료 일시 (JWT의 exp 클레임)
 * @property token 원본 JWT 토큰 문자열
 */
class UserAuthentication(
    val id: String?,
    val username: String,
    val issuer: String,
    val issuedDateTime: LocalDateTime,
    val notBeforeDateTime: LocalDateTime,
    val expireDateTime: LocalDateTime,
    private val token: String,
) : AbstractAuthenticationToken(emptySet()) {
    override fun getName(): String = username
    override fun getCredentials(): String = token
    override fun getPrincipal(): Any = this
    override fun setAuthenticated(isAuthenticated: Boolean) {
        if (this.isAuthenticated && !isAuthenticated) {
            throw IllegalArgumentException("Cannot set this token to unauthenticated; it is already authenticated.")
        }
        super.setAuthenticated(isAuthenticated)
    }
    // AbstractAuthenticationToken.equals/hashCode 는 getPrincipal() 을 비교/해싱하는데,
    // principal 이 this 를 반환하므로 기본 구현은 무한 재귀 → StackOverflowError.
    // SecurityContext 에는 요청당 단일 인스턴스이므로 identity 비교로 대체한다.
    // 부모 AbstractAuthenticationToken 이 Java 규약상 equals(Object) 를 Any 비-null 로
    // 노출하므로 override 시그니처도 Any 비-null 로 맞춘다.
    override fun equals(other: Any): Boolean = this === other
    override fun hashCode(): Int = System.identityHashCode(this)
}
