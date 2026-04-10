package dev.sayaya.handbook.interfaces.authentication

import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebExceptionHandler
import reactor.core.publisher.Mono

/**
 * 인증 예외를 처리하는 전역 예외 핸들러
 *
 * 예외 체인에서 [AuthenticationException]을 찾아
 * 401 Unauthorized 응답으로 변환합니다.
 */
@Order(-2)
class AuthorizationExceptionHandler : WebExceptionHandler {
    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        // 예외 체인에서 AuthenticationException 찾기
        var currentEx: Throwable? = ex
        while (currentEx != null) {
            if (currentEx is AuthenticationException) {
                return exchange.response.run {
                    statusCode = HttpStatus.UNAUTHORIZED
                    setComplete()
                }
            }
            currentEx = currentEx.cause
        }
        // 찾지 못하면 예외를 다음 핸들러로 전파
        return Mono.error(ex)
    }
}