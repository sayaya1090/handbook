package dev.sayaya.handbook.usecase

import com.fasterxml.jackson.databind.ObjectMapper
import dev.sayaya.handbook.domain.SystemRole
import dev.sayaya.handbook.domain.User
import dev.sayaya.handbook.interfaces.config.TokenFactoryConfig
import io.jsonwebtoken.Jwts
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank
import java.security.KeyPairGenerator
import java.util.*

class TokenFactoryTest : BehaviorSpec({

    // RSA 키 쌍 생성
    val keyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
    val privateKeyPem = run {
        val encoded = Base64.getEncoder().encodeToString(keyPair.private.encoded)
        "-----BEGIN PRIVATE KEY-----\n$encoded\n-----END PRIVATE KEY-----"
    }

    val config = TokenFactoryConfig().apply {
        secret = privateKeyPem
        duration = 3600
        publisher = "test-handbook"
    }
    val objectMapper = ObjectMapper()
    val factory = TokenFactory(config, objectMapper)

    Given("유효한 사용자 정보로 토큰 발급") {
        val user = User(
            id = UUID.randomUUID(),
            provider = "google",
            account = "test@example.com",
            name = "Test User",
            roles = mutableListOf(SystemRole.USER),
        )

        When("publish를 호출하면") {
            val token = factory.publish(user)

            Then("비어 있지 않은 JWT 문자열이 반환된다") {
                token.shouldNotBeBlank()
            }

            Then("RSA 공개키로 검증하면 올바른 클레임이 포함되어 있다") {
                val claims = Jwts.parser()
                    .clockSkewSeconds(86400)
                    .verifyWith(keyPair.public as java.security.interfaces.RSAPublicKey)
                    .build()
                    .parseSignedClaims(token)
                    .payload

                claims.issuer shouldBe "test-handbook"
                claims.id shouldBe user.id.toString()
                claims["name"] shouldBe "Test User"
                @Suppress("UNCHECKED_CAST")
                val authorities = claims["authorities"] as List<String>
                authorities shouldContain "USER"
            }

            Then("만료 시간이 설정되어 있다") {
                val claims = Jwts.parser()
                    .clockSkewSeconds(86400)
                    .verifyWith(keyPair.public as java.security.interfaces.RSAPublicKey)
                    .build()
                    .parseSignedClaims(token)
                    .payload

                claims.expiration shouldNotBe null
                claims.notBefore shouldNotBe null
                claims.expiration.after(claims.notBefore) shouldBe true
            }
        }
    }

    Given("여러 역할을 가진 사용자") {
        val user = User(
            id = UUID.randomUUID(),
            provider = "github",
            account = "admin@example.com",
            name = "Admin User",
            roles = mutableListOf(SystemRole.ADMIN, SystemRole.USER),
        )

        When("publish를 호출하면") {
            val token = factory.publish(user)

            Then("모든 역할이 authorities 클레임에 포함된다") {
                val claims = Jwts.parser()
                    .clockSkewSeconds(86400)
                    .verifyWith(keyPair.public as java.security.interfaces.RSAPublicKey)
                    .build()
                    .parseSignedClaims(token)
                    .payload

                @Suppress("UNCHECKED_CAST")
                val authorities = claims["authorities"] as List<String>
                authorities shouldContain "ADMIN"
                authorities shouldContain "USER"
            }
        }
    }

    Given("역할이 없는 사용자") {
        val user = User(
            id = UUID.randomUUID(),
            provider = "google",
            account = "norole@example.com",
            name = "No Role User",
        )

        When("publish를 호출하면") {
            val token = factory.publish(user)

            Then("빈 authorities 클레임으로 토큰이 생성된다") {
                val claims = Jwts.parser()
                    .clockSkewSeconds(86400)
                    .verifyWith(keyPair.public as java.security.interfaces.RSAPublicKey)
                    .build()
                    .parseSignedClaims(token)
                    .payload

                @Suppress("UNCHECKED_CAST")
                val authorities = claims["authorities"] as List<String>
                authorities.size shouldBe 0
            }
        }
    }
})
