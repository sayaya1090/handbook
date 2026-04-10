package dev.sayaya.handbook.interfaces.authentication

import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * WWW-Authenticate 헤더를 포함하지 않는 인증 진입점
 *
 * 인증 실패 시 401 Unauthorized 응답을 반환하되,
 * WWW-Authenticate 헤더를 생략하여 브라우저의 기본 인증 팝업을 방지합니다.
 */
class NoWwwAuthenticateEntryPoint : ServerAuthenticationEntryPoint {
    override fun commence(exchange: ServerWebExchange, ex: AuthenticationException): Mono<Void> {
        exchange.response.statusCode = HttpStatus.UNAUTHORIZED
        return exchange.response.setComplete()
    }
}
