package dev.sayaya.handbook.interfaces.authentication

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Header
import io.jsonwebtoken.Jwts
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.*
import org.springframework.http.ResponseCookie
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.util.MultiValueMap
import org.springframework.web.server.ServerWebExchange
import reactor.test.StepVerifier

internal class ExpiredTokenExceptionHandlerTest : DescribeSpec({

    val mockConfig = AuthenticationConfig().apply {
        header = "Test-Token"
        refresh = "Test-Refresh-Token"
    }
    val handler = ExpiredTokenExceptionHandler(mockConfig)

    val mockExchange = mockk<ServerWebExchange>()
    val mockResponse = mockk<ServerHttpResponse>()
    val mockCookies = mockk<MultiValueMap<String, ResponseCookie>>(relaxed = true)

    beforeEach {
        // 각 테스트 전에 모의 객체의 모든 상호작용 기록을 초기화합니다.
        clearMocks(mockExchange, mockResponse, mockCookies)

        every { mockExchange.response } returns mockResponse
        every { mockResponse.cookies } returns mockCookies
    }

    describe("handle 메소드는") {

        context("ExpiredJwtException이 직접 발생했을 때") {
            val exception = ExpiredJwtException(mockk<Header>(), Jwts.claims().build(), "Token expired")

            it("토큰과 리프레시 쿠키를 삭제하고 예외를 전파한다") {
                val result = handler.handle(mockExchange, exception)

                StepVerifier.create(result)
                    .expectErrorMatches { it === exception }
                    .verify()

                verify(exactly = 1) { mockCookies.add(mockConfig.header, any()) }
                verify(exactly = 1) { mockCookies.add(mockConfig.refresh, any()) }
            }
        }

        context("ExpiredJwtException이 다른 예외에 의해 래핑되었을 때") {
            val expiredException = ExpiredJwtException(mockk<Header>(), Jwts.claims().build(), "Token expired")
            val wrapperException = RuntimeException("Wrapper exception", expiredException)

            it("토큰과 리프레시 쿠키를 삭제하고 예외를 전파한다") {
                val result = handler.handle(mockExchange, wrapperException)

                StepVerifier.create(result)
                    .expectErrorMatches { it === wrapperException }
                    .verify()

                verify(exactly = 1) { mockCookies.add(mockConfig.header, any()) }
                verify(exactly = 1) { mockCookies.add(mockConfig.refresh, any()) }
            }
        }

        context("관련 없는 다른 예외가 발생했을 때") {
            val otherException = IllegalStateException("Some other error")

            it("아무것도 하지 않고 예외를 그대로 전파한다") {
                val result = handler.handle(mockExchange, otherException)

                StepVerifier.create(result)
                    .expectErrorMatches { it === otherException }
                    .verify()

                verify(exactly = 0) { mockCookies.add(any(), any()) }
            }
        }
    }
})