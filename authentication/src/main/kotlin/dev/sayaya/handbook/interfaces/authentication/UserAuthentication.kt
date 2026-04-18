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
 * ### sub 와 id(jti) 의 역할 구분 (Phase 1a — 2026-04-18)
 * - [sub] : **사용자 식별자** (내부 `user.id` UUID). 재발급 시에도 불변.
 *   소비자(persist-workspace 등)는 항상 이 값으로 `user_id` 를 참조한다.
 * - [id] : **토큰 식별자** (JWT `jti`). 매 토큰 발행마다 고유. 감사/블랙리스트용.
 *
 * Phase 1a 는 additive 변경 — [id] 필드를 남겨 둠으로써 기존 소비자가 깨지지 않도록
 * 하되, 다음 Phase 에서 소비자들이 [sub] 로 전환된 뒤 [id] 는 순수하게 토큰 ID 의미만
 * 갖도록 deprecation 예정.
 *
 * @property sub 사용자 UUID (JWT의 sub 클레임 — 영구 식별자, 재발급 불변). Phase 1a 에서
 *   backward compat 을 위해 nullable. 소비자가 전환되면 non-null 로 강화 예정.
 * @property id JWT jti 클레임. Phase 1a 이전 토큰에서는 사용자 UUID 가 담겨 있고, 이후
 *   토큰은 매 발행 고유 토큰 ID 가 담긴다. 신규 코드는 [sub] 사용 권장.
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
    val sub: String? = null,
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
