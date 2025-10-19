package dev.sayaya.handbook.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.throwable.shouldHaveMessage
import java.security.KeyPairGenerator
import java.util.Base64

internal class PemTest : FunSpec({

    context("Pem 클래스는 PEM 형식의 문자열로부터 PublicKey를 생성한다") {

        test("올바른 Public Key PEM 문자열이 주어지면 PublicKey 객체를 성공적으로 생성한다") {
            val tokenConfig = TokenConfig().apply { secret = RSA_PUBLIC_KEY_PEM }
            val pem = Pem(tokenConfig)

            pem.public shouldNotBe null
            pem.public.algorithm shouldBe "RSA"
        }

        test("올바른 Private Key PEM 문자열이 주어지면 PublicKey 객체를 성공적으로 추출하여 생성한다") {
            val tokenConfig = TokenConfig().apply { secret = RSA_PRIVATE_KEY_PEM }
            val pem = Pem(tokenConfig)

            pem.public shouldNotBe null
            pem.public.algorithm shouldBe "RSA"
        }

        context("잘못된 PEM이 주어졌을 때") {
            test("PEM 형식이 아니면 IllegalArgumentException 예외를 발생시킨다") {
                val invalidPem = "this-is-not-pem"
                val tokenConfig = TokenConfig().apply { secret = invalidPem }

                val exception = shouldThrow<IllegalArgumentException> {
                    Pem(tokenConfig)
                }
                exception.message shouldContain "is not PEM encoded data"
            }

            test("지원하지 않는 타입이면 IllegalArgumentException 예외를 발생시킨다") {
                val unsupportedPem = """
                    |-----BEGIN CERTIFICATE-----
                    |MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAy8Dbv8prpJ/h5/02364s
                    |-----END CERTIFICATE-----
                """.trimMargin()
                val tokenConfig = TokenConfig().apply { secret = unsupportedPem }

                val exception = shouldThrow<IllegalArgumentException> {
                    Pem(tokenConfig)
                }
                exception shouldHaveMessage "CERTIFICATE is not a supported format"
            }
        }
    }
}) {
    companion object {
        private val keyPair = KeyPairGenerator.getInstance("RSA").apply {
            initialize(2048)
        }.generateKeyPair()

        private val encoder = Base64.getMimeEncoder(64, "\n".toByteArray())

        val RSA_PUBLIC_KEY_PEM: String
        val RSA_PRIVATE_KEY_PEM: String

        init {
            val publicContent = encoder.encodeToString(keyPair.public.encoded)
            RSA_PUBLIC_KEY_PEM = """
                |-----BEGIN PUBLIC KEY-----
                |$publicContent
                |-----END PUBLIC KEY-----
            """.trimMargin().also { println("Generated Public Key for Test:\n$it") }

            val privateContent = encoder.encodeToString(keyPair.private.encoded)
            RSA_PRIVATE_KEY_PEM = """
                |-----BEGIN PRIVATE KEY-----
                |$privateContent
                |-----END PRIVATE KEY-----
            """.trimMargin().also { println("Generated Private Key for Test:\n$it") }
        }
    }
}