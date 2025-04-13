package dev.sayaya.handbook.domain

import org.bouncycastle.util.encoders.Base64
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PublicKey
import java.security.interfaces.RSAPrivateCrtKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPublicKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.regex.Pattern

@Component
class KeyPair(config: TokenConfig) {
    private val pem = Pattern.compile("-----BEGIN (.*)-----(.*)-----END (.*)-----", Pattern.DOTALL)
    val public: PublicKey = pemToPublicKey(config.secret)
    private fun pemToPublicKey(pemData: String): PublicKey {
        val m = pem.matcher(pemData.trim())
        require(m.matches()) { "$pemData is not PEM encoded data" }
        val type = m.group(1)
        val content = Base64.decode(m.group(2).toByteArray(StandardCharsets.UTF_8))
        return when (type) {
            "PUBLIC KEY" -> {
                val keySpec = X509EncodedKeySpec(content)
                KeyFactory.getInstance("RSA").generatePublic(keySpec)
            }
            "PRIVATE KEY" -> {
                val keySpec = PKCS8EncodedKeySpec(content)
                val privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpec) as RSAPrivateCrtKey
                val publicKeySpec = RSAPublicKeySpec(privateKey.modulus, privateKey.publicExponent)
                KeyFactory.getInstance("RSA").generatePublic(publicKeySpec)
            } else -> throw IllegalArgumentException("$type is not a supported format")
        }
    }
}