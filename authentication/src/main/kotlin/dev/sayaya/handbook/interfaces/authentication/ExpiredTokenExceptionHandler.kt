package dev.sayaya.handbook.interfaces.authentication

import io.jsonwebtoken.ExpiredJwtException
import org.springframework.boot.web.server.Cookie.SameSite.LAX
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebExceptionHandler
import reactor.core.publisher.Mono

@Component
@Order(-3) // AuthenticationExceptionHandler보다 먼저 실행되도록 더 낮은 값 설정
class ExpiredTokenExceptionHandler(private val config: AuthenticationConfig) : WebExceptionHandler {
    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        // 예외 체인에서 ExpiredJwtException 찾기
        var currentEx: Throwable? = ex
        while (currentEx != null) {
            if (currentEx is ExpiredJwtException) {
                val clearToken = ResponseCookie.from(config.header).maxAge(0).path("/").httpOnly(true).secure(true).sameSite(LAX.attributeValue()).build()
                exchange.response.cookies.add(config.header, clearToken)
                val clearRefreshCookie = ResponseCookie.from(config.refresh).maxAge(0).path("/").httpOnly(true).secure(true).sameSite(LAX.attributeValue()).build()
                exchange.response.cookies.add(config.refresh, clearRefreshCookie)
                return Mono.error(ex)
            }
            currentEx = currentEx.cause
        }
        return Mono.error(ex)
    }
}
