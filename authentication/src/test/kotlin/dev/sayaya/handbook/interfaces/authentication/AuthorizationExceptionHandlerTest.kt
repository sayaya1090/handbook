package dev.sayaya.handbook.interfaces.authentication

import io.kotest.core.spec.style.DescribeSpec
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

internal class AuthorizationExceptionHandlerTest : DescribeSpec({

    val exceptionHandler = AuthorizationExceptionHandler()
    val mockExchange = mockk<ServerWebExchange>()
    val mockResponse = mockk<ServerHttpResponse>(relaxed = true)

    beforeEach {
        // 이전에 기록된 모든 상호작용을 지워 각 테스트를 완벽하게 격리합니다.
        clearMocks(mockExchange, mockResponse)

        every { mockExchange.response } returns mockResponse
        every { mockResponse.setComplete() } returns Mono.empty()
    }

    describe("handle 메소드는") {

        context("AuthenticationException이 직접 발생했을 때") {
            it("응답을 401 UNAUTHORIZED로 설정하고 완료한다") {
                val exception = mockk<AuthenticationException>()
                val result = exceptionHandler.handle(mockExchange, exception)

                StepVerifier.create(result).verifyComplete()
                verify(exactly = 1) { mockResponse.statusCode = HttpStatus.UNAUTHORIZED }
                verify(exactly = 1) { mockResponse.setComplete() }
            }
        }

        context("AuthenticationException이 다른 예외에 의해 래핑되었을 때") {
            it("응답을 401 UNAUTHORIZED로 설정하고 완료한다") {
                val authException = mockk<AuthenticationException>()
                val wrapperException = RuntimeException("Wrapper", authException)
                val result = exceptionHandler.handle(mockExchange, wrapperException)

                StepVerifier.create(result).verifyComplete()
                verify(exactly = 1) { mockResponse.statusCode = HttpStatus.UNAUTHORIZED }
                verify(exactly = 1) { mockResponse.setComplete() }
            }
        }

        context("관련 없는 다른 예외가 발생했을 때") {
            it("예외를 그대로 전파하고 응답을 변경하지 않는다") {
                val exception = RuntimeException("Something else went wrong")
                val result = exceptionHandler.handle(mockExchange, exception)

                StepVerifier.create(result)
                    .expectErrorMatches { it === exception }
                    .verify()

                verify(exactly = 0) { mockResponse.statusCode = any() }
            }
        }
    }
})