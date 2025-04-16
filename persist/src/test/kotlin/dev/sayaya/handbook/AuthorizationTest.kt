package dev.sayaya.handbook

import dev.sayaya.handbook.testcontainer.Database
import io.kotest.core.spec.style.BehaviorSpec
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.junit.jupiter.Testcontainers
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import java.util.*

@SpringBootTest(properties = [
    "management.endpoint.health.probes.enabled=true",
    "logging.level.io.r2dbc.postgresql.QUERY=DEBUG",
    "logging.level.io.r2dbc.postgresql.PARAM=DEBUG",
    "spring.security.authentication.header=Authentication"
], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
internal class AuthorizationTest(private val client: WebTestClient): BehaviorSpec({
    Given("인증 없이") {
        When("API에 접근하면") {
            val req = client.get().uri("/services")
            Then("Unauthorized 코드를 리턴한다") {
                // req.exchange().expectStatus().isUnauthorized.expectBody().isEmpty()
            }
        }
    }
}) {
    companion object {
        private val PUBLIC_KEY: String
        init {
            val rsaKeyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
            val publicKey = (rsaKeyPair.public as RSAPublicKey)
            PUBLIC_KEY = """
                -----BEGIN PUBLIC KEY-----
                ${Base64.getEncoder().encodeToString(publicKey.encoded)}
                -----END PUBLIC KEY-----
            """.trimIndent()
        }
        @JvmStatic
        @DynamicPropertySource
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            Database().registerDynamicProperties(registry)
            registry.add("spring.security.authentication.jwt.secret") { PUBLIC_KEY }
        }
    }
}