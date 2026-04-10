package dev.sayaya.handbook.interfaces.authentication

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.spec.style.StringSpec
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

internal class NoWwwAuthenticateEntryPointTest : StringSpec({
    val entryPoint = NoWwwAuthenticateEntryPoint()
    val mockExchange = mockk<ServerWebExchange>()
    val mockResponse = mockk<ServerHttpResponse>(relaxed = true)
    beforeEach {
        clearMocks(mockExchange, mockResponse)
        every { mockExchange.response } returns mockResponse
        every { mockResponse.setComplete() } returns Mono.empty()
    }

    "commence 메소드는 응답 상태 코드를 401로 설정하고 응답을 완료한다" {
        // Arrange
        val mockException = mockk<AuthenticationException>()

        every { mockExchange.response } returns mockResponse
        every { mockResponse.setComplete() } returns Mono.empty()

        // Act
        val result = entryPoint.commence(mockExchange, mockException)

        // Assert
        StepVerifier.create(result).verifyComplete()

        verify(exactly = 1) { mockResponse.statusCode = HttpStatus.UNAUTHORIZED }
        verify(exactly = 1) { mockResponse.setComplete() }
    }
})