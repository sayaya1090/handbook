package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.interfaces.authentication.UserAuthentication
import dev.sayaya.handbook.interfaces.config.AuthenticationCookieService
import dev.sayaya.handbook.usecase.TokenPublisher
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * JWT 토큰 갱신 컨트롤러.
 *
 * **책임:** 인증된 사용자의 기존 토큰을 검증하고 새 JWT를 발급하여 쿠키로 설정한다.
 * SPA에서 토큰 만료 전에 주기적으로 호출하여 세션을 유지한다.
 *
 * **의존관계:**
 * - [TokenPublisher] — 토큰 검증 및 재발급
 * - [AuthenticationCookieService] — 인증 쿠키 설정
 */
@RestController
class TokenRefreshController(
    private val tokenPublisher: TokenPublisher,
    private val cookieService: AuthenticationCookieService,
) {
    @GetMapping("/auth/refresh")
    @ResponseStatus(HttpStatus.OK)
    fun refresh(
        @AuthenticationPrincipal authentication: UserAuthentication,
        exchange: ServerWebExchange,
    ): Mono<Void> {
        return tokenPublisher.validateRefreshToken(authentication)
            .flatMap { token -> cookieService.sendAuthenticationCookie(exchange, token) }
    }
}
