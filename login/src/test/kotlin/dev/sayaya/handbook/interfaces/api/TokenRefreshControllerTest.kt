package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.interfaces.authentication.UserAuthentication
import dev.sayaya.handbook.interfaces.config.AuthenticationCookieService
import dev.sayaya.handbook.usecase.TokenPublisher
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime

class TokenRefreshControllerTest : DescribeSpec({
    val tokenPublisher = mockk<TokenPublisher>()
    val cookieService = mockk<AuthenticationCookieService>()
    val controller = TokenRefreshController(tokenPublisher, cookieService)

    describe("TokenRefreshController") {
        it("refresh: 토큰을 검증하고 새 쿠키를 설정한다") {
            val now = LocalDateTime.now()
            val auth = UserAuthentication(
                id = "jti",
                username = "alice",
                issuer = "handbook",
                issuedDateTime = now,
                notBeforeDateTime = now,
                expireDateTime = now.plusHours(1),
                token = "old-token",
                sub = "user-uuid"
            )
            val mockExchange = mockk<ServerWebExchange>()
            
            every { tokenPublisher.validateRefreshToken(auth) } returns Mono.just("new-token")
            every { cookieService.sendAuthenticationCookie(mockExchange, "new-token") } returns Mono.empty()

            StepVerifier.create(controller.refresh(auth, mockExchange))
                .verifyComplete()

            verify { tokenPublisher.validateRefreshToken(auth) }
            verify { cookieService.sendAuthenticationCookie(mockExchange, "new-token") }
        }
    }
})
