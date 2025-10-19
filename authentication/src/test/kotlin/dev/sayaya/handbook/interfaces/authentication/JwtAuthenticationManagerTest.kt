package dev.sayaya.handbook.interfaces.authentication

import dev.sayaya.handbook.domain.Pem
import io.jsonwebtoken.Jwts
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.TestingAuthenticationToken
import reactor.test.StepVerifier
import java.security.KeyPairGenerator
import java.time.Instant
import java.util.*

internal class JwtAuthenticationManagerTest : DescribeSpec({

    val keyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
    val mockPem = mockk<Pem>()
    val mockConverter = mockk<ClaimsAuthenticationConverter>()

    // JwtAuthenticationManager 생성 전에 모의 객체의 동작을 미리 설정합니다.
    every { mockPem.public } returns keyPair.public

    val authenticationManager = JwtAuthenticationManager(mockPem, mockConverter)

    // 임시 인증 토큰을 만드는 헬퍼 클래스
    class JwtTestToken(private val jwt: String) : org.springframework.security.authentication.AbstractAuthenticationToken(null) {
        override fun getCredentials(): Any = jwt
        override fun getPrincipal(): Any = jwt
    }

    beforeEach {
        // 각 테스트 전에 모의 객체의 호출 기록과 동작 설정을 초기화합니다.
        clearMocks(mockConverter, answers = false, recordedCalls = true)
    }

    describe("authenticate 메소드는") {

        context("유효한 JWT 토큰이 주어졌을 때") {
            it("토큰을 파싱하고, 변환을 요청한 뒤, 인증된 Authentication 객체를 반환한다") {
                // Arrange
                val validJwt = Jwts.builder().subject("test").signWith(keyPair.private).compact()
                val initialToken = JwtTestToken(validJwt)
                val expectedAuth = TestingAuthenticationToken("user", "cred")

                every { mockConverter.convert(any(), validJwt) } returns expectedAuth

                // Act
                val result = authenticationManager.authenticate(initialToken)

                // Assert
                StepVerifier.create(result)
                    .assertNext { auth ->
                        auth shouldBe expectedAuth
                        auth.isAuthenticated shouldBe true
                    }
                    .verifyComplete()

                verify(exactly = 1) { mockConverter.convert(any(), validJwt) }
            }
        }

        context("만료된 JWT 토큰이 주어졌을 때") {
            it("ExpiredJwtException을 포함한 Mono.error를 반환하고, 변환기는 호출하지 않는다") {
                val expiredJwt = Jwts.builder()
                    .expiration(Date.from(Instant.now().minusSeconds(10)))
                    .signWith(keyPair.private)
                    .compact()
                val token = JwtTestToken(expiredJwt)

                val result = authenticationManager.authenticate(token)

                StepVerifier.create(result)
                    .expectError(io.jsonwebtoken.ExpiredJwtException::class.java)
                    .verify()

                verify(exactly = 0) { mockConverter.convert(any(), any()) }
            }
        }

        context("서명이 잘못된 JWT 토큰이 주어졌을 때") {
            it("BadCredentialsException을 포함한 Mono.error를 반환하고, 변환기는 호출하지 않는다") {
                val wrongKeyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair()
                val invalidJwt = Jwts.builder().subject("test").signWith(wrongKeyPair.private).compact()
                val token = JwtTestToken(invalidJwt)

                val result = authenticationManager.authenticate(token)

                StepVerifier.create(result)
                    .expectError(BadCredentialsException::class.java)
                    .verify()

                verify(exactly = 0) { mockConverter.convert(any(), any()) }
            }
        }

        context("null 인증 객체가 주어졌을 때") {
            it("빈 Mono를 반환하고, 변환기는 호출하지 않는다") {
                val result = authenticationManager.authenticate(null)

                StepVerifier.create(result).verifyComplete()

                verify(exactly = 0) { mockConverter.convert(any(), any()) }
            }
        }
    }
})