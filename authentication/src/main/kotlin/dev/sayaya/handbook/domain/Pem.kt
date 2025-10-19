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
 * PEM 형식의 키 데이터를 파싱하여 공개 키를 생성하는 컴포넌트
 *
 * PEM 형식의 공개 키 또는 개인 키를 파싱하여 RSA 공개 키로 변환합니다.
 * 개인 키가 제공된 경우에는 해당 개인 키로부터 공개 키를 추출합니다.
 *
 * @property public JWT 검증에 사용될 RSA 공개 키
 */
class Pem(config: TokenConfig) {
    val public: PublicKey = toPublicKey(config.secret)
    
    /**
     * PEM 형식의 문자열을 PublicKey 객체로 변환
     *
     * @param pemData PEM 형식의 키 데이터 (PUBLIC KEY 또는 PRIVATE KEY)
     * @return 파싱된 RSA 공개 키
     * @throws IllegalArgumentException PEM 형식이 올바르지 않거나 지원하지 않는 형식인 경우
     */
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
            } else -> throw IllegalArgumentException("$type is not a supported format")
        }
    }
    companion object {
        private val pem = Pattern.compile("-----BEGIN (.*)-----(.*)-----END (.*)-----", Pattern.DOTALL)
    }
}