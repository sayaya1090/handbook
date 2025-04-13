package dev.sayaya.handbook.interfaces.authentication

import dev.sayaya.handbook.domain.Pem
import dev.sayaya.handbook.domain.TokenConfig
import dev.sayaya.handbook.usecase.authentication.UserAuthentication
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.Jwts
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import reactor.test.StepVerifier
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.util.*

internal class JwtAuthenticationManagerTest: ShouldSpec({
    context("RSA 키페어를 사용한 토큰 인증") {
        val rsaKeyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        val tokenConfig = TokenConfig().apply { secret = (rsaKeyPair.public as RSAPublicKey).pemKey() }
        val pem = Pem(tokenConfig)
        val jwtManager = JwtAuthenticationManager(pem)

        val claims = mapOf(
            "jti" to "test-id",
            "name" to "test-user",
            "iss" to "https://issuer.example.com",
            "aud" to setOf("https://audience.example.com"),
            "iat" to Instant.now().epochSecond,
            "nbf" to Instant.now().epochSecond,
            "exp" to Instant.now().plusSeconds(3600).epochSecond // 1 hour expiration
        )

        should("유효한 토큰으로 인증에 성공해야 한다") {
            val validToken = claims.jwtBuilder().signWith(rsaKeyPair.private, Jwts.SIG.RS256).compact()
            val authentication = mockk<Authentication> {
                every { credentials } returns validToken
            }
            jwtManager.authenticate(authentication).let(StepVerifier::create).consumeNextWith { auth ->
                val userAuth = auth as UserAuthentication
                userAuth.isAuthenticated shouldBe true
                auth.isAuthenticated shouldBe true
            }.verifyComplete()
        }

        should("만료된 토큰으로 인증에 실패해야 한다") {
            val expiredToken = claims.toMutableMap().apply {
                put("exp", Instant.now().minusSeconds(1000).epochSecond) // 만료된 시간 설정
            }.jwtBuilder().signWith(rsaKeyPair.private, Jwts.SIG.RS256).compact()

            val authentication = mockk<Authentication> {
                every { credentials } returns expiredToken
            }
            jwtManager.authenticate(authentication).let(StepVerifier::create).verifyError(BadCredentialsException::class.java)
        }

        should("null 인증 객체를 전달하면 빈 결과여야 한다") {
            jwtManager.authenticate(null).let(StepVerifier::create).verifyComplete()
        }

        should("유효하지 않은 토큰으로 인증에 실패해야 한다") {
            val fakeToken = "invalid.token.content"
            val authentication = mockk<Authentication> {
                every { credentials } returns fakeToken
            }
            jwtManager.authenticate(authentication).let(StepVerifier::create).verifyError(BadCredentialsException::class.java)
        }
    }
}) {
    companion object {
        private fun RSAPublicKey.pemKey(): String = """
                -----BEGIN PUBLIC KEY-----
                ${Base64.getEncoder().encodeToString(this.encoded)}
                -----END PUBLIC KEY-----
            """.trimIndent()
        private fun Map<String, Any>.jwtBuilder(): JwtBuilder = Jwts.builder()
            .id(this["jti"] as String)
            .subject(this["name"] as String)
            .issuer(this["iss"] as String)
            .issuedAt(Date((this["iat"] as Long) * 1000))
            .notBefore(Date((this["nbf"] as Long) * 1000))
            .expiration(Date((this["exp"] as Long) * 1000))
            .claims(mapOf("name" to this["name"]))
    }
}