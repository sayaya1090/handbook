package dev.sayaya.handbook.interfaces.config

import dev.sayaya.handbook.interfaces.authentication.AuthenticationConfig
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * 인증 쿠키의 생성과 삭제를 담당하는 서비스.
 *
 * **책임:** JWT 토큰을 HttpOnly/Secure 쿠키로 설정하거나 삭제한다.
 * OAuth2 로그인 성공, 토큰 갱신, 로그아웃 시 사용된다.
 *
 * **의존관계:**
 * - [AuthenticationConfig] — 쿠키 이름(header)
 * - [TokenFactoryConfig] — JWT 만료 시간(maxAge)
 *
 * **주의:** 쿠키는 항상 HttpOnly=true, Secure=true, SameSite=Strict, Path="/"로 설정된다.
 */
@Component
class AuthenticationCookieService(
    private val authConfig: AuthenticationConfig,
    private val tokenFactoryConfig: TokenFactoryConfig,
) {
    /**
     * JWT 토큰을 인증 쿠키로 설정한다.
     *
     * @param exchange 현재 서버 교환 객체
     * @param token 설정할 JWT 토큰 문자열
     * @return 빈 Mono (쿠키 설정 후 완료)
     */
    fun sendAuthenticationCookie(exchange: ServerWebExchange, token: String): Mono<Void> {
        val cookie = ResponseCookie.from(authConfig.header, token)
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .path("/")
            .maxAge(tokenFactoryConfig.duration)
            .build()
        exchange.response.addCookie(cookie)
        return Mono.empty()
    }

    /**
     * 인증 쿠키를 삭제한다 (maxAge=0으로 설정).
     *
     * @param exchange 현재 서버 교환 객체
     * @return 빈 Mono (쿠키 삭제 후 완료)
     */
    fun clearAuthenticationCookie(exchange: ServerWebExchange): Mono<Void> {
        val cookie = ResponseCookie.from(authConfig.header, "")
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .path("/")
            .maxAge(0)
            .build()
        exchange.response.addCookie(cookie)
        return Mono.empty()
    }
}
