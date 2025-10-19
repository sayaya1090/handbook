package dev.sayaya.handbook.interfaces.authentication

import dev.sayaya.handbook.domain.TokenConfig
import io.jsonwebtoken.Jwts
import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.util.*
import java.util.Date

@WebFluxTest(properties = [
    "spring.security.authentication.header=Authorization"
]) @ContextConfiguration(classes = [ AuthenticationAutoConfigTest.Companion.TestConfig::class ])
@ApplyExtension(SpringExtension::class)
internal class AuthenticationAutoConfigTest(
    private val webTestClient: WebTestClient,
    private val config: AuthenticationConfig,
    private val keyPair: KeyPair
) : DescribeSpec({
    describe("SecurityFilterChain은") {
        context("인증 토큰이 없는 요청이 보호된 경로로 들어오면") {
            it("401 Unauthorized를 반환하고 WWW-Authenticate 헤더가 없다") {
                webTestClient.get().uri("/api/test")
                    .exchange()
                    .expectStatus().isUnauthorized
                    .expectHeader().doesNotExist(HttpHeaders.WWW_AUTHENTICATE)
            }
        }
        context("유효한 토큰이 있는 요청이 보호된 경로로 들어오면") {
            it("200 OK를 반환한다") {
                val validJwt = Jwts.builder()
                    .subject("test")
                    .claim("name", "testUser")
                    .issuer("test-issuer")
                    .issuedAt(Date())
                    .notBefore(Date())
                    .expiration(Date(System.currentTimeMillis() + 3600000))
                    .signWith(keyPair.private)
                    .compact()

                webTestClient.get().uri("/api/test")
                    .cookie(config.header, validJwt)
                    .exchange()
                    .expectStatus().isOk
            }
        }
    }
}) {
    companion object {
        val TEST_KEY_PAIR: KeyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        val TEST_PUBLIC_KEY: String = """
            -----BEGIN PUBLIC KEY-----
            ${Base64.getMimeEncoder(64, "\n".toByteArray()).encodeToString(TEST_KEY_PAIR.public.encoded)}
            -----END PUBLIC KEY-----
        """.trimIndent()

        @SpringBootConfiguration
        @Import(AuthenticationAutoConfig::class)
        class TestConfig {
            @Bean fun keyPair(): KeyPair = TEST_KEY_PAIR
            @Primary @Bean fun tokenConfig(): TokenConfig = TokenConfig().apply {
                secret = TEST_PUBLIC_KEY
            }
            @Bean fun testController() = TestController()
        }
        @RestController
        class TestController {
            @GetMapping("/api/test")
            fun testEndpoint() = "OK"
        }
    }
}