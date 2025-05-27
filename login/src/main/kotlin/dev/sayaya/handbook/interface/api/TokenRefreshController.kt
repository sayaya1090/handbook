package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.usecase.TokenPublisher
import dev.sayaya.handbook.usecase.authentication.UserAuthentication
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@RestController
internal class TokenRefreshController(
    private val tokenPublisher: TokenPublisher,
    private val config: SecurityConfig
) {
    @GetMapping(value = ["/auth/refresh"])
    @ResponseStatus(HttpStatus.OK)
    fun refresh(authentication: UserAuthentication, exchange: ServerWebExchange): Mono<Void> = tokenPublisher
        .validateRefreshToken(authentication).flatMap { token ->
            with(config) {
                exchange.sendAuthenticationCookie(token)
            }
        }
}