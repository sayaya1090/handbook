package dev.sayaya.handbook

import dev.sayaya.handbook.testcontainer.Database
import io.kotest.core.spec.style.BehaviorSpec
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.r2dbc.core.DatabaseClient
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
internal class IntegrationTest(
    private val _client: WebTestClient,
    private val db: DatabaseClient
): BehaviorSpec({
    val client = _client.mutate().build()
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