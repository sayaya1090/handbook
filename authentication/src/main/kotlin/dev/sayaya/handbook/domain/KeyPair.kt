package dev.sayaya.handbook.domain

import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.pkcs.RSAPrivateKey
import org.bouncycastle.asn1.pkcs.RSAPublicKey
import org.bouncycastle.util.encoders.Base64
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.RSAPublicKeySpec
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
            "RSA PUBLIC KEY" -> {
                val seq = ASN1Sequence.getInstance(content)
                require(seq.size() == 2) { "Invalid RSA Public Key ASN1 sequence." }
                val key = RSAPublicKey.getInstance(seq)
                val pubSpec = RSAPublicKeySpec(key.modulus, key.publicExponent)
                KeyFactory.getInstance("RSA").generatePublic(pubSpec)
            }
            "RSA PRIVATE KEY" -> {
                val seq = ASN1Sequence.getInstance(content)
                require(seq.size() == 9) { "Invalid RSA Private Key ASN1 sequence." }
                val key = RSAPrivateKey.getInstance(seq)
                val pubSpec = RSAPublicKeySpec(key.modulus, key.publicExponent)
                KeyFactory.getInstance("RSA").generatePublic(pubSpec)
            } else -> throw IllegalArgumentException("$type is not a supported format")
        }
    }
}