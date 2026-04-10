package dev.sayaya.handbook.interfaces.config

import dev.sayaya.handbook.interfaces.authentication.AuthenticationConfig
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpCookie
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import reactor.test.StepVerifier

class AuthenticationCookieServiceTest : BehaviorSpec({

    val authConfig = AuthenticationConfig().apply {
        header = "Authorization"
    }
    val tokenFactoryConfig = TokenFactoryConfig().apply {
        duration = 3600
    }
    val service = AuthenticationCookieService(authConfig, tokenFactoryConfig)

    Given("인증 쿠키 설정") {
        When("sendAuthenticationCookie를 호출하면") {
            val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))
            val token = "test-jwt-token-value"

            Then("HttpOnly, Secure, SameSite=Strict, Path=/ 쿠키가 설정된다") {
                StepVerifier.create(service.sendAuthenticationCookie(exchange, token))
                    .verifyComplete()

                val cookies = exchange.response.cookies
                val cookie = cookies.getFirst("Authorization")
                cookie shouldNotBe null
                cookie!!.value shouldBe token
                cookie.isHttpOnly shouldBe true
                cookie.isSecure shouldBe true
                cookie.sameSite shouldBe "Strict"
                cookie.path shouldBe "/"
                cookie.maxAge.seconds shouldBe 3600L
            }
        }
    }

    Given("인증 쿠키 삭제") {
        When("clearAuthenticationCookie를 호출하면") {
            val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))

            Then("maxAge=0인 빈 쿠키가 설정된다") {
                StepVerifier.create(service.clearAuthenticationCookie(exchange))
                    .verifyComplete()

                val cookies = exchange.response.cookies
                val cookie = cookies.getFirst("Authorization")
                cookie shouldNotBe null
                cookie!!.value shouldBe ""
                cookie.isHttpOnly shouldBe true
                cookie.isSecure shouldBe true
                cookie.sameSite shouldBe "Strict"
                cookie.path shouldBe "/"
                cookie.maxAge.seconds shouldBe 0L
            }
        }
    }

    Given("다른 쿠키 이름으로 설정") {
        val customAuthConfig = AuthenticationConfig().apply {
            header = "X-Auth-Token"
        }
        val customService = AuthenticationCookieService(customAuthConfig, tokenFactoryConfig)

        When("sendAuthenticationCookie를 호출하면") {
            val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))
            val token = "custom-token"

            Then("설정된 쿠키 이름으로 생성된다") {
                StepVerifier.create(customService.sendAuthenticationCookie(exchange, token))
                    .verifyComplete()

                val cookie = exchange.response.cookies.getFirst("X-Auth-Token")
                cookie shouldNotBe null
                cookie!!.value shouldBe "custom-token"
            }
        }

        When("clearAuthenticationCookie를 호출하면") {
            val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))

            Then("설정된 쿠키 이름으로 삭제된다") {
                StepVerifier.create(customService.clearAuthenticationCookie(exchange))
                    .verifyComplete()

                val cookie = exchange.response.cookies.getFirst("X-Auth-Token")
                cookie shouldNotBe null
                cookie!!.maxAge.seconds shouldBe 0L
            }
        }
    }
})
