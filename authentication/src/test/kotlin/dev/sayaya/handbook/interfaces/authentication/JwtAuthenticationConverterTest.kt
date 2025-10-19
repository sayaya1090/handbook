package dev.sayaya.handbook.interfaces.authentication

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpCookie
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.server.ServerWebExchange
import reactor.test.StepVerifier

internal class JwtAuthenticationConverterTest : DescribeSpec({

    val mockConfig = AuthenticationConfig().apply {
        header = "My-Auth-Cookie"
    }
    val converter = JwtAuthenticationConverter(mockConfig)

    // 모의 객체 준비
    val mockExchange = mockk<ServerWebExchange>()
    val mockRequest = mockk<ServerHttpRequest>()

    beforeEach {
        every { mockExchange.request } returns mockRequest
    }

    describe("convert 메소드는") {

        context("요청에 올바른 인증 쿠키가 존재할 때") {
            it("JWT를 담은 Authentication 토큰을 Mono로 반환한다") {
                // Arrange
                val jwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.sSflK-zSt1l5Pef3D_xV5g"
                val cookie = HttpCookie(mockConfig.header, jwt)
                every { mockRequest.cookies.getFirst(mockConfig.header) } returns cookie

                // Act
                val result = converter.convert(mockExchange)

                // Assert
                StepVerifier.create(result)
                    .assertNext { authentication ->
                        authentication.principal shouldBe jwt
                        authentication.credentials shouldBe jwt
                    }
                    .verifyComplete()
            }
        }

        context("요청에 올바른 인증 쿠키가 존재하지 않을 때") {
            it("빈 Mono를 반환한다") {
                // Arrange
                every { mockRequest.cookies.getFirst(mockConfig.header) } returns null

                // Act
                val result = converter.convert(mockExchange)

                // Assert
                StepVerifier.create(result)
                    .verifyComplete() // 아무것도 방출하지 않고 완료되는지 검증
            }
        }

        context("요청에 쿠키가 전혀 없을 때") {
            it("빈 Mono를 반환한다") {
                // Arrange
                every { mockRequest.cookies } returns LinkedMultiValueMap()

                // Act
                val result = converter.convert(mockExchange)

                // Assert
                StepVerifier.create(result)
                    .verifyComplete()
            }
        }
    }
})