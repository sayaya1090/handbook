package dev.sayaya.handbook.usecase

import tools.jackson.databind.ObjectMapper
import dev.sayaya.handbook.domain.Token
import dev.sayaya.handbook.domain.User
import dev.sayaya.handbook.interfaces.config.TokenFactoryConfig
import io.jsonwebtoken.Jwts
import org.bouncycastle.util.encoders.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Instant
import java.util.*
import java.util.regex.Pattern

/**
 * JWT 토큰 생성 팩토리.
 *
 * **책임:** [User] 도메인 객체를 받아 RSA 서명된 JWT 문자열을 생성한다.
 * PEM 형식의 개인키를 파싱하여 JJWT 라이브러리로 서명한다.
 *
 * **의존관계:**
 * - [TokenFactoryConfig] — RSA 개인키, 만료 시간, 발행자 설정
 * - [ObjectMapper] — (현재 미사용, 향후 확장용)
 *
 * **주의:**
 * - PEM 개인키는 PKCS#8 형식이어야 한다. PKCS#1 형식은 지원하지 않는다.
 * - `iat/nbf/exp` 는 반드시 [Instant.now] (UTC epoch) 기반으로 계산한다.
 *   `LocalDateTime` 과 `toInstant(ZoneOffset.UTC)` 조합을 사용하면 pod TZ 영향으로
 *   시간이 미래로 찍혀 `PrematureJwtException` 이 발생한다 (2026-04 dev 관찰).
 */
class TokenFactory(
    private val config: TokenFactoryConfig,
    private val objectMapper: ObjectMapper,
) {
    private val privateKey: PrivateKey = pemToPrivateKey(config.secret)

    fun publish(user: User): String {
        val now = Instant.now()
        val exp = now.plusSeconds(config.duration)
        val token = user.toToken(nbf = now, exp = exp, iss = config.publisher, iat = now)
        return sign(token)
    }

    private fun sign(token: Token): String {
        val claims = mapOf(
            "authorities" to token.authorities,
            "name" to token.name,
        )
        return Jwts.builder()
            .id(token.id)
            .issuer(token.iss)
            .issuedAt(Date.from(token.iat))
            .notBefore(Date.from(token.nbf))
            .expiration(Date.from(token.exp))
            .claims(claims)
            .signWith(privateKey)
            .compact()
    }

    private fun pemToPrivateKey(pemData: String): PrivateKey {
        val m = pem.matcher(pemData.trim())
        require(m.matches()) { "$pemData is not PEM encoded data" }
        val content = Base64.decode(m.group(2).toByteArray(StandardCharsets.UTF_8))
        val keySpec = PKCS8EncodedKeySpec(content)
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec)
    }

    companion object {
        private val pem = Pattern.compile("-----BEGIN (.*)-----(.*)-----END (.*)-----", Pattern.DOTALL)
    }
}
