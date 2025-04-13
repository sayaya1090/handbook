package dev.sayaya.handbook.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*

internal class KeyPairTest : ShouldSpec({
    context("RSA 키 테스트") {
        val rsaKeyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        val privateKey = rsaKeyPair.private // PrivateKey 가져오기
        val publicKey = (rsaKeyPair.public as RSAPublicKey) // RSAPublicKey 가져오기

        should("PEM 포맷의 Private 키에서 Public 키를 올바르게 생성해야 한다") {
            val tokenConfig = TokenConfig().apply { secret = privateKey.pemKey() }
            val keyPair = KeyPair(tokenConfig)

            val generatedPublicKey = keyPair.public as RSAPublicKey
            generatedPublicKey.modulus shouldBe publicKey.modulus
            generatedPublicKey.publicExponent shouldBe publicKey.publicExponent
        }

        should("PEM 포맷의 Public 키에서 Public 키를 올바르게 반환해야 한다") {
            val tokenConfig = TokenConfig().apply { secret = publicKey.pemKey() }
            val keyPair = KeyPair(tokenConfig)

            val generatedPublicKey = keyPair.public as RSAPublicKey
            generatedPublicKey.modulus shouldBe publicKey.modulus
            generatedPublicKey.publicExponent shouldBe publicKey.publicExponent
        }

        should("유효하지 않은 PEM 데이터로 예외를 발생시켜야 한다") {
            val invalidPem = """
                -----BEGIN INVALID KEY-----
                ${Base64.getEncoder().encodeToString("InvalidData".toByteArray())}
                -----END INVALID KEY-----
            """.trimIndent()
            val tokenConfig = TokenConfig().apply { secret = invalidPem }
            val exception = shouldThrow<IllegalArgumentException> {
                KeyPair(tokenConfig)
            }
            exception.message shouldBe "INVALID KEY is not a supported format"
        }
    }
}) {
    companion object {
        private fun PrivateKey.pemKey(): String = """
                -----BEGIN PRIVATE KEY-----
                ${Base64.getEncoder().encodeToString(this.encoded)}
                -----END PRIVATE KEY-----
            """.trimIndent()
        private fun RSAPublicKey.pemKey(): String = """
                -----BEGIN PUBLIC KEY-----
                ${Base64.getEncoder().encodeToString(this.encoded)}
                -----END PUBLIC KEY-----
            """.trimIndent()
    }
}