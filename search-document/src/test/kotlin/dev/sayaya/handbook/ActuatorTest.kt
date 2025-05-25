package dev.sayaya.handbook

import dev.sayaya.handbook.testcontainer.Database
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import java.util.*

@SpringBootTest(properties = [
    "management.endpoint.health.probes.enabled=true",
    "management.endpoints.web.exposure.include=health,info,prometheus",
    "management.endpoint.health.show-details=always",
    "management.prometheus.metrics.export.enabled=true",
    "logging.level.io.r2dbc.postgresql.QUERY=DEBUG",
    "logging.level.io.r2dbc.postgresql.PARAM=DEBUG",
    "spring.security.authentication.header=Authentication"
], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
internal class ActuatorTest(
    private val client: WebTestClient
): BehaviorSpec({
    Given("액추에이터가 활성화되어 서버 기동") {
        When("액추에이터 readiness 엔드포인트를 요청하면") {
            val req = client.get().uri("/actuator/health/readiness")
            Then("OK 코드와 응답을 리턴한다") {
                req.exchange().expectStatus().isOk
                    .expectBody(String::class.java)
                    .returnResult().responseBody shouldBe "{\"status\":\"UP\"}"
            }
        }
        When("액추에이터 liveness 엔드포인트를 요청하면") {
            val req = client.get().uri("/actuator/health/liveness")
            Then("OK 코드와 응답을 리턴한다") {
                req.exchange().expectStatus().isOk
                    .expectBody(String::class.java)
                    .returnResult().responseBody shouldBe "{\"status\":\"UP\"}"
            }
        }
    }
    Given("프로메테우스 설정이 활성화되어 서버 기동") {
        When("액추에이터 readiness 엔드포인트를 요청하면") {
            val req = client.get().uri("/actuator/prometheus")
            Then("OK 코드와 응답을 리턴한다") {
                val response = req.exchange().expectStatus().isOk
                    .expectBody(String::class.java)
                    .returnResult().responseBody

                response shouldContain "jvm_memory_used_bytes"
                response shouldContain "process_uptime_seconds"
                response shouldContain "http_server_requests_seconds_count"
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