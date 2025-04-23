package dev.sayaya.handbook.interfaces.authentication

import dev.sayaya.handbook.domain.Pem
import dev.sayaya.handbook.usecase.authentication.UserAuthentication
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationManager (pem: Pem): ReactiveAuthenticationManager {
    private val parser: JwtParser = Jwts.parser().verifyWith(pem.public).build()
    override fun authenticate(authentication: Authentication?): Mono<Authentication> {
        if(authentication==null) return Mono.empty()
        val token = authentication.credentials as String
        return try {
            val claims = parser.parseSignedClaims(token).payload
            UserAuthentication(claims, token).let {
                it.isAuthenticated = true
                Mono.just(it)
            }
        } catch (e: ExpiredJwtException) {
            Mono.error(e)
        } catch (e: Exception) {
            e.printStackTrace()
            Mono.error(BadCredentialsException("Invalid token"))
        }
    }
}