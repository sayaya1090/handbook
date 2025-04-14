package dev.sayaya.handbook

import dev.sayaya.handbook.testcontainer.Database
import dev.sayaya.handbook.testcontainer.OAuthServer
import dev.sayaya.handbook.testcontainer.OAuthServer.Companion.PROVIDER
import dev.sayaya.handbook.testcontainer.OAuthServer.Companion.USER
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.test.StepVerifier
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAPrivateCrtKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPublicKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.Duration
import java.util.*
import java.util.regex.Pattern

@SpringBootTest(properties=[
    "spring.security.authentication.header=${ IntegrationTest.AUTHENTICATION }",
    "spring.security.authentication.login-redirect-uri=index.html",
    "spring.security.authentication.logout-redirect-uri=login.html",
    "spring.security.authentication.jwt.signature-algorithm=RS256",
    "spring.security.authentication.jwt.duration=3600",
    "spring.security.authentication.jwt.publisher=${ IntegrationTest.PUBLISHER }",
    "spring.security.authentication.jwt.client=${ IntegrationTest.CLIENT }"
])
@AutoConfigureWebTestClient
@Testcontainers
internal class IntegrationTest(
    private val client: WebTestClient,
    private val databaseClient: DatabaseClient
): BehaviorSpec({
    beforeSpec {
        databaseClient.sql("""
            INSERT INTO public."user" (id, last_modified_at, created_at, last_login_at, name, provider, account) VALUES ('93951bc3-be1e-4fc8-865f-d6376ac3e87b', NOW(), NOW(), null, 'system', '$PROVIDER', '$USER');
        """.trimIndent()
        ).fetch().rowsUpdated().let(StepVerifier::create).expectNextCount(1).verifyComplete()
    }
    Given("인증이 안 된 상태에서") {
        When("메뉴를 요청하면") {
            val request = client.get().uri("/menus").exchange()
            Then("로그인 메뉴를 출력한다") {
                request.expectStatus().isOk
                    .expectBody().jsonPath("$[0].title").isEqualTo("sign in")
            }
        }
        When("새로운 사용자로 OAuth2 로그인 요청하면") {
            val publishToken = client.login(USER)
            Then("쿠키로 JWT 토큰이 발급된다") {
                publishToken.expectCookie().exists(AUTHENTICATION)
                    .expectCookie().httpOnly(AUTHENTICATION, true)
                    .expectCookie().secure(AUTHENTICATION, true)
                    .expectCookie().path(AUTHENTICATION, "/")
                    .expectCookie().sameSite(AUTHENTICATION, "LAX")
            }
            Then("JWT 토큰을 decrypt 할 수 있고 그 값으로 원래 사용자의 권한 등 토큰 정보를 확인할 수 있다") {
                val token = publishToken.returnResult<Void>().responseCookies[AUTHENTICATION]!!.first().value
                val decrypt = jwtParser(PUBLIC_KEY).parseSignedClaims(token)
                val claims = decrypt.payload
                claims["authorities"] shouldNotBe null
                val authorities = claims["authorities"].shouldBeInstanceOf<List<String>>()
                // authorities shouldContain "ROLE_USER"
                claims.issuer shouldBeEqual PUBLISHER
                claims.notBefore shouldNotBe null
            }
            Then("loginRedirectUri로 리다이렉트 요청으로 응답한다") {
                publishToken.expectStatus().isFound
                    .expectHeader().location("index.html")
            }
            When("인증 쿠키와 함께 메뉴를 요청하면") {
                val token = publishToken.returnResult<Void>().responseCookies[AUTHENTICATION]!!.first().value
                val request = client.get().uri("/menus").cookie(AUTHENTICATION, token).exchange()
                Then("로그아웃 메뉴를 출력한다") {
                    request.expectStatus().isOk
                        .expectBody().jsonPath("$[0].title").isEqualTo("sign out")
                }
            }
            When("로그아웃을 시도하면") {
                val token = publishToken.returnResult<Void>().responseCookies[AUTHENTICATION]!!.first().value
                val logout = client.post().uri("/oauth2/logout").cookie(AUTHENTICATION, token).exchange()
                Then("쿠키 만료") {
                    logout.expectCookie().maxAge(AUTHENTICATION, Duration.ofSeconds(0))
                        .expectCookie().httpOnly(AUTHENTICATION, true)
                        .expectCookie().secure(AUTHENTICATION, true)
                        .expectCookie().path(AUTHENTICATION, "/")
                }
                Then("logoutRedirectUri로 리다이렉트") {
                    logout.expectStatus().isFound
                        .expectHeader().location("login.html")
                }
            }
        }
    }
    Given("Admin 계정 생성") {
        val userCnt = databaseClient.countUser()
        When("기존에 존재하는 사용자(admin)로 OAuth2 로그인 요청하면") {
            val publishToken = client.login(USER)
            Then("JWT 토큰을 decrypt 하여 Admin 권한을 확인할 수 있다") {
                val token = publishToken.returnResult<Void>().responseCookies[AUTHENTICATION]!!.first().value
                val decrypt = jwtParser(PUBLIC_KEY).parseSignedClaims(token)
                val claims = decrypt.payload
                claims["authorities"] shouldNotBe null
                val authorities = claims["authorities"].shouldBeInstanceOf<List<String>>()
                // authorities shouldContain "ROLE_ADMIN"
            }
            Then("사용자는 추가되지 않는다") {
                databaseClient.countUser() shouldBe userCnt
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
            val publicKey = (rsaKeyPair.public as RSAPublicKey)
            PRIVATE_KEY = privateKey.pemKey()
            PUBLIC_KEY = publicKey.pemKey()
        }
        fun jwtParser(publicKey: String): JwtParser = Jwts.parser().verifyWith(pemToPublicKey(publicKey)).build()
        const val AUTHENTICATION = "Authentication"
        const val PUBLISHER = "publisher.test"
        const val CLIENT = "client.test"
        @JvmStatic
        @DynamicPropertySource
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            Database().registerDynamicProperties(registry)
            OAuthServer().registerDynamicProperties(registry)
            registry.add("spring.security.authentication.jwt.secret") { PRIVATE_KEY }
        }
        fun DatabaseClient.countUser(): Long = sql("SELECT count(*) from \"user\"").fetch().one().mapNotNull { it.values.first() as Long }.block()!!
        fun WebTestClient.login(username: String): WebTestClient.ResponseSpec {
            val request = get().uri("/oauth2/authorization/$PROVIDER").exchange()
            val loginUrl by lazy { request.returnResult<Any>().responseHeaders.location }
            val session by lazy { request.returnResult<Any>().responseCookies["SESSION"]!!.first().value }
            val authentication = WebClient.create().post().uri(loginUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(LinkedMultiValueMap<String, String>().apply {
                    set("username", username)
                }).retrieve().toBodilessEntity().map { response ->
                    response.statusCode shouldBe HttpStatus.FOUND
                    response.headers.location
                }.block()
            return get().uri(checkNotNull(authentication)).cookie("SESSION", session).exchange()
        }
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
    }
}