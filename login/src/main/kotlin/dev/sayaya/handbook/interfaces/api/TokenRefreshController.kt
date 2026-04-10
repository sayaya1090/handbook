package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.interfaces.authentication.UserAuthentication
import dev.sayaya.handbook.interfaces.config.LoginSecurityConfig
import dev.sayaya.handbook.usecase.TokenPublisher
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@RestController
class TokenRefreshController(
    private val tokenPublisher: TokenPublisher,
    private val securityConfig: LoginSecurityConfig,
) {
    @GetMapping("/auth/refresh")
    @ResponseStatus(HttpStatus.OK)
    fun refresh(
        @AuthenticationPrincipal authentication: UserAuthentication,
        exchange: ServerWebExchange,
    ): Mono<Void> {
        return tokenPublisher.validateRefreshToken(authentication)
            .flatMap { token -> securityConfig.sendAuthenticationCookie(exchange, token) }
    }
}
