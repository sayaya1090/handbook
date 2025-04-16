package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.SystemRole
import dev.sayaya.handbook.domain.TokenConfig
import dev.sayaya.handbook.domain.User
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAPrivateCrtKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPublicKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import java.util.regex.Pattern

internal class TokenFactoryTest: BehaviorSpec({
    val om = JsonConfig().objectMapper()
    val user = User(id=UUID.randomUUID(), provider="", account="", name="", roles= mutableListOf(SystemRole.USER))

    Given("지원하는 알고리즘으로 토큰 팩토리 생성 후에") {
        val tokenConfig = TokenConfig().apply {
            secret = PRIVATE_KEY
        }
        val config = TokenFactoryConfig().apply {
            signatureAlgorithm = "RS256"
            publisher = "test-publisher"
            client = "test-client"
            duration = 60000L
        }
        val factory = TokenFactory(tokenConfig, config, om)
        When("사용자 정보로 토큰 생성을 요청하면") {
            val token = factory.publish(user)
            Then("JWT 토큰이 발급된다") {
                token shouldNotBe null
            }
            Then("JWT 토큰을 decrypt 할 수 있고 그 값으로 원래 사용자의 정보를 확인할 수 있다") {
                val decrypt = jwtParser(PUBLIC_KEY).parseSignedClaims(token)
                val claims = decrypt.payload

                user.id.toString() shouldBeEqual claims["name"]!!
                claims["authorities"] shouldNotBe null
                val authorities = claims["authorities"].shouldBeInstanceOf<List<String>>()
                authorities shouldContain "ROLE_USER"
                claims.issuer shouldBeEqual config.publisher
                claims.notBefore shouldNotBe null
            }
        }
    }
    Given("지원되지 않는 알고리즘으로 토큰 팩토리 생성 후에") {
        val tokenConfig = TokenConfig().apply {
            secret = PRIVATE_KEY
        }
        val config = TokenFactoryConfig().apply {
            signatureAlgorithm = "Invalid Algorithm"
            publisher = "test-publisher"
            client = "test-client"
            duration = 60000L
        }
        val factory = TokenFactory(tokenConfig, config, om)
        When("사용자 정보로 토큰 생성을 요청하면") {
            Then("IllegalArgumentException 예외를 발생시킨다") {
                shouldThrow<IllegalArgumentException> {
                    factory.publish(user)
                }
            }
        }
    }
    Given("잘못된 키페어가 주어지면") {
        val tokenConfig = TokenConfig().apply {
            secret = ILLEGAL_PRIVATE_KEY
        }
        val config = TokenFactoryConfig().apply {
            signatureAlgorithm = "RS256"
            publisher = "test-publisher"
            client = "test-client"
            duration = 60000L
        }
        Then("IllegalArgumentException 예외를 발생시킨다") {
            shouldThrow<IllegalArgumentException> {
                val factory = TokenFactory(tokenConfig, config, om)
                factory.publish(user)
            }
        }
    }
}) {
    companion object {
        private val PRIVATE_KEY: String
        private val PUBLIC_KEY: String
        init {
            val rsaKeyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
            val privateKey = rsaKeyPair.private
            val publicKey = (rsaKeyPair.public as java.security.interfaces.RSAPublicKey)
            PRIVATE_KEY = privateKey.pemKey()
            PUBLIC_KEY = publicKey.pemKey()
        }
        private fun PrivateKey.pemKey(): String = """
                -----BEGIN PRIVATE KEY-----
                ${java.util.Base64.getEncoder().encodeToString(this.encoded)}
                -----END PRIVATE KEY-----
            """.trimIndent()
        private fun java.security.interfaces.RSAPublicKey.pemKey(): String = """
                -----BEGIN PUBLIC KEY-----
                ${java.util.Base64.getEncoder().encodeToString(this.encoded)}
                -----END PUBLIC KEY-----
            """.trimIndent()
        fun jwtParser(publicKey: String): JwtParser = Jwts.parser().verifyWith(pemToPublicKey(publicKey)).build()
        private val pem = Pattern.compile("-----BEGIN (.*)-----(.*)-----END (.*)-----", Pattern.DOTALL)
        private fun pemToPublicKey(pemData: String): PublicKey {
            val m = pem.matcher(pemData.trim())
            require(m.matches()) { "$pemData is not PEM encoded data" }
            val type = m.group(1)
            val content = org.bouncycastle.util.encoders.Base64.decode(m.group(2).toByteArray(StandardCharsets.UTF_8))
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
        val ILLEGAL_PRIVATE_KEY = """
            -----BEGIN RSA PRIVATE KEY-----
            MIIEowIBAAKCAQEAv0q4ej+Hf7LulM4hMwaJZ86APEOLEPqm2FNaToI4zV7z/pVi
            MC6iPzf4zHNjF6JWrm3S5cngEmw3I9ElcoQrd8/ndRveZqOkNF8uch7x0cOJC6ox
            GIxvtmO1Ec/J8HhYPmr+it1Z2Rh5YQqa7NfA4AR/TmoJd/pV1X/pCwF0InlUCIHI
            AOTTzcJBnT3XuFmsKPWCPdNjC+YRtRULX/q656weT0POfV13jbmz6UoQwF6PL6pK
            tBfvtRzd1eA+Pc47vJaHTy3hLPmpVU9UuRFJ1C5u6QeyKP21J23rygevjpnlVQOe
            e3WQv+v5pPwsagGfkAAIbebLrMRm5X5NwLXAOQIDAQABAoIBAQCBdw3B1ytalvx4
            A8ZeZWcrtYv+vWvqcunm8QrmpaXSARi7zdilaXpvtO8TWGjRfxKRfUzGLsoTTeBH
            wm5IwgE1VqV9Ef6Eku44b87cd+sMH/2pwmb2CV42H+dVhb9Tm++FVx6tV0BO+Qx1
            TBssfp1QQFr09DkyVWAwXiCYTUSPa0Y7mOoyp0p8q96tCH7gKYUpXO5mdTuNjRVn
            lIfHPWjxpiAFH2VVOj8jqMLrzyLnixAiouemAzCBs83aHuDO0/gRO/iN104L9n/o
            QIPOvecFI3+MqU0JzJYSNEz0Zi+o1uTgZc6gUicPVeFH9cBTQPd6EKgVO8LOPaeH
            2e4RYYttAoGBAPrGsO2DQ2zWy7lxPoZMRm5xwiCogwoWMiQ7ubm9SZ7TaTQzreIY
            ISR0gzpM2KwD0QTWXoPO6ZpuwqO8FGcNSGosce/8nE+ZPb7CYBBfobFHo2YkM++Y
            elNsKTz9QAGXCbk5zF/ROnuTniAKhBDcZUZ4zICSwtTTKyDmFNXScFMjAoGBAMNG
            0agrp0Voo4FWva2RZ1fbH2mqw2eTIyWcWDkl20I8Jgv5Yuw8a9QikbQgT/6sUsOf
            TjMJ2VfTlQNwL/YRwuT6Q/GY8tIu2Uzjm5tppmqDrMbkBgzCDxWfV9OLM7xDYDwh
            5TfsSE2jxWfMswi/zIxfCjGKyfkW4Qt2/0hjXjLzAoGATI122Spm3MS9MADX21tR
            bMmhPyLxzZR0/gaVbZPQ84EJ7nuQKyK+i0hd/uASjIAlwFpIQ+hX+2KwXBdACy1M
            28xxg5cTiGD5LlBbzuPCkkGSKc4HZK6hOPIdrJaKgXG/8CEquF1AgxTPAmzzX8pH
            yDl8BAvJGfrUgZh658Lzsw0CgYBUUV3x6Xd+huIi1Ntt+JzQ2LLFo5BgRq4kbU/C
            zU/RV7tt7C8EpkpA/PRA/LrN0oaiJUVU0GnifF+ZbnWnIKAw0sdHqK0giE4X3yev
            gXzz/Qs7jfX2yExPH9CCbVbXcZg6HsCk1weZTp/MZBziKD8gVWFHZxAy1+gwVR+B
            mDZydQKBgANuScImHgEfdN3VuC8qnA9p43LdCMLNBhHyYnvnBS172w5w+v+gSTyD
            RJeIvupO6Mxa4c0npg9aI8Ksb4D9rhCyHft6+pk+k4pFF31GqypQq1Ogqc6QF1DL
            jwfHWb/mT84fvow0oR6Yqg7bzmpXIIqZ+m7em3SaHXIc6cMFoodO
            -----END RSA PRIVATE KEY-----
        """.trimIndent()
    }
}