package dev.sayaya.handbook.interfaces.authentication

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * HTTP 쿠키에서 JWT 토큰을 추출하여 인증 객체로 변환하는 컨버터
 *
 * 요청의 쿠키에서 JWT 토큰을 읽어 Spring Security의 Authentication 객체로 변환합니다.
 * 실제 토큰 검증은 [JwtAuthenticationManager]에서 수행됩니다.
 */
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