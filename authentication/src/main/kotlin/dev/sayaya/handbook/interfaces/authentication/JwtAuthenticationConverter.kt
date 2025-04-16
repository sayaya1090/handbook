package dev.sayaya.handbook.interfaces.authentication

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationConverter (
    private val config: AuthenticationConfig,
): ServerAuthenticationConverter {
    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        val request = exchange.request
        val authentication = request.cookies.getFirst(config.header)?.value ?.let { JwtAuthenticationToken(it) }
        return Mono.justOrEmpty(authentication)
    }
    private class JwtAuthenticationToken(private val jwt: String): AbstractAuthenticationToken(emptySet()) {
        override fun getCredentials(): String = jwt
        override fun getPrincipal(): String = jwt
    }
}