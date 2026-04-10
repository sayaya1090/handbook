package dev.sayaya.handbook.interfaces.config

import dev.sayaya.handbook.interfaces.authentication.AuthenticationConfig
import dev.sayaya.handbook.usecase.TokenPublisher
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler
import java.time.Duration

class LoginSecurityConfigTest : BehaviorSpec({
    val authConfig = mockk<AuthenticationConfig>()
    val urlConfig = mockk<AuthenticationUrlConfig>()
    val tokenPublisher = mockk<TokenPublisher>()
    val tokenFactoryConfig = mockk<TokenFactoryConfig>()

    every { authConfig.header } returns "Authorization"
    every { tokenFactoryConfig.duration } returns 3600L
    every { urlConfig.loginRedirectUri } returns "/"
    every { urlConfig.logoutRedirectUri } returns "/login"

    val config = LoginSecurityConfig(authConfig, urlConfig, tokenPublisher, tokenFactoryConfig)

    // UC-L3: 로그아웃 - 인증 쿠키 설정 검증
    Given("사용자 인증 쿠키 설정 요청이 주어졌을 때") {
        val token = "jwt-token-value"
        val request = MockServerHttpRequest.get("/").build()
        val exchange = MockServerWebExchange.from(request)

        When("sendAuthenticationCookie를 호출하면") {
            config.sendAuthenticationCookie(exchange, token).block()

            Then("응답에 인증 쿠키가 추가된다") {
                val cookies = exchange.response.cookies
                val authCookies = cookies["Authorization"]
                authCookies shouldNotBe null
                authCookies!!.size shouldBe 1

                val cookie = authCookies[0]
                cookie.value shouldBe "jwt-token-value"
                cookie.isHttpOnly shouldBe true
                cookie.isSecure shouldBe true
                cookie.sameSite shouldBe "Lax"
                cookie.path shouldBe "/"
                cookie.maxAge.seconds shouldBe 3600L
            }
        }
    }

    // UC-L3: 로그아웃 - 쿠키 제거 핸들러 검증
    Given("로그아웃 성공 핸들러가 동작할 때") {
        // logoutSuccessHandler는 private이므로 리플렉션으로 접근하여 검증
        val method = LoginSecurityConfig::class.java.getDeclaredMethod("logoutSuccessHandler")
        method.isAccessible = true
        val handler = method.invoke(config) as ServerLogoutSuccessHandler

        val request = MockServerHttpRequest.post("/oauth2/logout").build()
        val exchange = MockServerWebExchange.from(request)

        When("logoutSuccessHandler가 실행되면") {
            val webFilterExchange = mockk<org.springframework.security.web.server.WebFilterExchange>()
            every { webFilterExchange.exchange } returns exchange
            handler.onLogoutSuccess(webFilterExchange, mockk<org.springframework.security.core.Authentication>(relaxed = true)).block()

            Then("인증 쿠키가 maxAge=0으로 제거된다") {
                val cookies = exchange.response.cookies
                val authCookies = cookies["Authorization"]
                authCookies shouldNotBe null
                authCookies!!.size shouldBe 1

                val cookie = authCookies[0]
                cookie.value shouldBe ""
                cookie.maxAge shouldBe Duration.ZERO
                cookie.isHttpOnly shouldBe true
                cookie.isSecure shouldBe true
                cookie.path shouldBe "/"
            }

            Then("로그아웃 후 리다이렉트가 설정된다") {
                exchange.response.statusCode?.value() shouldBe 302
                exchange.response.headers.location?.toString() shouldBe "/login"
            }
        }
    }
})
