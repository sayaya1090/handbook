package dev.sayaya.handbook.interfaces.authentication

import dev.sayaya.handbook.domain.Pem
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import reactor.core.publisher.Mono

/**
 * JWT 토큰을 검증하고 인증 객체를 생성하는 매니저
 *
 * JWT 토큰의 서명을 검증하고, 유효한 경우 Claims를 파싱하여
 * [ClaimsAuthenticationConverter]를 통해 인증 객체로 변환합니다.
 *
 * @property converter JWT Claims를 Authentication으로 변환하는 컨버터
 * @see ClaimsAuthenticationConverter
 * @see UserAuthenticationConverter
 */
class JwtAuthenticationManager (
    pem: Pem,
    private val converter: ClaimsAuthenticationConverter
): ReactiveAuthenticationManager {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val parser: JwtParser = Jwts.parser().verifyWith(pem.public).build()
    override fun authenticate(authentication: Authentication?): Mono<Authentication> {
        if(authentication==null) return Mono.empty()
        val token = authentication.credentials as String
        return try {
            val claims = parser.parseSignedClaims(token).payload
            // 변환 책임을 컨버터에 위임
            converter.convert(claims, token).let {
                it.isAuthenticated = true
                Mono.just(it)
            }
        } catch (e: ExpiredJwtException) {
            Mono.error(e)
        } catch (e: Exception) {
            logger.warn("Invalid token", e)
            Mono.error(BadCredentialsException("Invalid token"))
        }
    }
}