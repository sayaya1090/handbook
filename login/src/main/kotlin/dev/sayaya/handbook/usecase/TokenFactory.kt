package dev.sayaya.handbook.usecase

import com.fasterxml.jackson.databind.ObjectMapper
import dev.sayaya.handbook.domain.Token
import dev.sayaya.handbook.domain.TokenConfig
import dev.sayaya.handbook.domain.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.SecureDigestAlgorithm
import org.bouncycastle.util.encoders.Base64
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.regex.Pattern

@Service
class TokenFactory(
    tokenConfig: TokenConfig,
    private val config: TokenFactoryConfig,
    private val objectMapper: ObjectMapper
) {
    private val pem = Pattern.compile("-----BEGIN (.*)-----(.*)-----END (.*)-----", Pattern.DOTALL)
    private val privateKey = pemToPrivateKey(tokenConfig.secret)
    private fun pemToPrivateKey(pemData: String): PrivateKey {
        val m = pem.matcher(pemData.trim())
        require(m.matches()) { "$pemData is not PEM encoded data" }
        val type = m.group(1)
        val content = Base64.decode(m.group(2).toByteArray(StandardCharsets.UTF_8))
        return when (type) {
            "PRIVATE KEY" -> {
                val keySpec = PKCS8EncodedKeySpec(content)
                KeyFactory.getInstance("RSA").generatePrivate(keySpec)
            } else -> throw IllegalArgumentException("$type is not a supported format")
        }
    }
    fun publish(user: User): String {
        val iat = LocalDateTime.now(ZoneId.of("UTC"))
        val payload = user.toToken(iat, iat + config.duration, config.publisher, iat)
        return sign(payload)
    }
    private fun sign(payload: Token): String {
        val signatureAlgorithm = Jwts.SIG.get()[config.signatureAlgorithm]
        if(signatureAlgorithm is SecureDigestAlgorithm) {
            return Jwts.builder()
                .header().add("typ", "JWT").and()
                .content(objectMapper.writeValueAsString(payload))
                .signWith(privateKey, signatureAlgorithm as SecureDigestAlgorithm<PrivateKey, *>)
                .compact()
        } else throw IllegalArgumentException("Unsupported algorithm: ${config.signatureAlgorithm}")
    }
    operator fun LocalDateTime.plus(millis: Long): LocalDateTime {
        return this.plus(millis, ChronoUnit.MILLIS)
    }
}