package dev.sayaya.handbook.domain

import org.bouncycastle.util.encoders.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PublicKey
import java.security.interfaces.RSAPrivateCrtKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPublicKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.regex.Pattern

/**
 * PEM 형식의 키 문자열을 파싱하여 RSA 공개 키를 생성하는 도메인 객체.
 *
 * 공개 키 또는 개인 키 PEM을 받아 공개 키로 변환한다.
 * 개인 키가 주어진 경우 해당 키로부터 공개 키를 추출한다.
 *
 * @param pemSecret PEM 형식의 키 문자열
 * @property public JWT 검증에 사용될 RSA 공개 키
 */
class Pem(pemSecret: String) {
    val public: PublicKey = toPublicKey(pemSecret)

    private fun toPublicKey(pemData: String): PublicKey {
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
            }
            else -> throw IllegalArgumentException("$type is not a supported format")
        }
    }

    companion object {
        private val pem = Pattern.compile("-----BEGIN (.*)-----(.*)-----END (.*)-----", Pattern.DOTALL)
    }
}
