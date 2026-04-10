package dev.sayaya.handbook.usecase

import com.fasterxml.jackson.databind.ObjectMapper
import dev.sayaya.handbook.domain.Token
import dev.sayaya.handbook.domain.User
import dev.sayaya.handbook.interfaces.config.TokenFactoryConfig
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.bouncycastle.util.encoders.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.regex.Pattern

class TokenFactory(
    private val config: TokenFactoryConfig,
    private val objectMapper: ObjectMapper,
) {
    private val privateKey: PrivateKey = pemToPrivateKey(config.secret)

    fun publish(user: User): String {
        val now = LocalDateTime.now()
        val nbf = now
        val exp = now.plusSeconds(config.duration)
        val token = user.toToken(nbf, exp, config.publisher, now)
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
            .issuedAt(Date.from(token.iat.toInstant(ZoneOffset.UTC)))
            .notBefore(Date.from(token.nbf.toInstant(ZoneOffset.UTC)))
            .expiration(Date.from(token.exp.toInstant(ZoneOffset.UTC)))
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
