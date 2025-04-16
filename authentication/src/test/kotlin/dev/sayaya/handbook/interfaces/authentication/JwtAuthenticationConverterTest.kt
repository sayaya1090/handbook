package dev.sayaya.handbook.interfaces.authentication

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpCookie
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.core.Authentication
import org.springframework.web.server.ServerWebExchange

internal class JwtAuthenticationConverterTest : ShouldSpec({
    context("쿠키에서 Authentication 추출 테스트") {
        val config = AuthenticationConfig().apply { header = "Authorization" }
        val jwtConverter = JwtAuthenticationConverter(config)

        should("요청에 유효한 Authorization 쿠키가 있는 경우 Authentication 객체를 반환해야 한다") {
            val jwtToken = "test.jwt.token"
            val httpCookie = mockk<HttpCookie> {
                every { value } returns jwtToken
            }
            val serverHttpRequest = mockk<ServerHttpRequest> {
                every { cookies.getFirst(config.header) } returns httpCookie
            }
            val exchange = mockk<ServerWebExchange> {
                every { request } returns serverHttpRequest
            }
            val authenticationMono = jwtConverter.convert(exchange)

            authenticationMono.block()!!.let { auth ->
                auth.shouldBeInstanceOf<Authentication>()
                auth.credentials shouldBe jwtToken
            }
        }

        should("요청에 Authorization 쿠키가 없을 경우 빈 Mono를 반환해야 한다") {
            val serverHttpRequest = mockk<ServerHttpRequest> {
                every { cookies.getFirst(config.header) } returns null
            }
            val exchange = mockk<ServerWebExchange> {
                every { request } returns serverHttpRequest
            }
            val authenticationMono = jwtConverter.convert(exchange)
            authenticationMono.blockOptional().isPresent shouldBe false
        }
    }
})