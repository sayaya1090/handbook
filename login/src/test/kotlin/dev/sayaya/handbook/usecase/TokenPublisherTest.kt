package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.SystemRole
import dev.sayaya.handbook.domain.User
import dev.sayaya.handbook.interfaces.authentication.UserAuthentication
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.oauth2.core.user.OAuth2User
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime
import java.util.*

class TokenPublisherTest : BehaviorSpec({
    val userRepository = mockk<UserRepository>()
    val tokenFactory = mockk<TokenFactory>()
    val publisher = TokenPublisher(userRepository, tokenFactory)

    Given("기존 사용자가 OAuth2로 로그인할 때") {
        val user = User(
            id = UUID.randomUUID(),
            provider = "google",
            account = "oauth-id-123",
            name = "Test User",
            roles = mutableListOf(SystemRole.USER),
        )
        val principal = mockk<OAuth2User>()
        every { principal.name } returns "oauth-id-123"
        every { userRepository.findUserByProviderAndAccount("google", "oauth-id-123") } returns Mono.just(user)
        every { userRepository.updateLastLoginDateTime(user.id, any()) } returns Mono.empty()
        every { tokenFactory.publish(user) } returns "jwt-token-string"

        When("publish를 호출하면") {
            val result = publisher.publish("google", principal)

            Then("JWT 토큰이 반환된다") {
                StepVerifier.create(result)
                    .expectNext("jwt-token-string")
                    .verifyComplete()
            }
            Then("마지막 로그인 시간이 갱신된다") {
                verify { userRepository.updateLastLoginDateTime(user.id, any()) }
            }
        }
    }

    Given("신규 사용자가 OAuth2로 로그인할 때") {
        val principal = mockk<OAuth2User>()
        every { principal.name } returns "new-oauth-id"
        every { principal.getAttribute<String>("name") } returns "New User"
        every { userRepository.findUserByProviderAndAccount("google", "new-oauth-id") } returns Mono.empty()
        every { userRepository.create(any()) } answers {
            Mono.just(firstArg())
        }
        every { tokenFactory.publish(any<User>()) } returns "new-jwt-token"

        When("publish를 호출하면") {
            val result = publisher.publish("google", principal)

            Then("새 사용자가 생성되고 JWT 토큰이 반환된다") {
                StepVerifier.create(result)
                    .expectNext("new-jwt-token")
                    .verifyComplete()
            }
            Then("사용자가 생성된다") {
                verify { userRepository.create(any()) }
            }
        }
    }

    Given("OAuth2 사용자에 name 속성이 없을 때") {
        val principal = mockk<OAuth2User>()
        every { principal.name } returns "no-name-oauth-id"
        every { principal.getAttribute<String>("name") } returns null
        every { userRepository.findUserByProviderAndAccount("github", "no-name-oauth-id") } returns Mono.empty()
        every { userRepository.create(any()) } answers {
            val created = firstArg<User>()
            Mono.just(created)
        }
        every { tokenFactory.publish(any<User>()) } returns "fallback-name-token"

        When("publish를 호출하면") {
            val result = publisher.publish("github", principal)

            Then("account ID가 표시 이름으로 사용되어 토큰이 발급된다") {
                StepVerifier.create(result)
                    .expectNext("fallback-name-token")
                    .verifyComplete()
            }
            Then("생성된 사용자의 name이 account와 동일하다") {
                verify { userRepository.create(match { it.name == "no-name-oauth-id" && it.account == "no-name-oauth-id" }) }
            }
        }
    }

    Given("Phase 1a 이후 토큰(sub 포함)으로 갱신할 때") {
        val userId = UUID.randomUUID()
        val jti = UUID.randomUUID()
        val user = User(
            id = userId,
            provider = "google",
            account = "oauth-id-123",
            name = "Test User",
            roles = mutableListOf(SystemRole.USER),
        )
        val authentication = UserAuthentication(
            id = jti.toString(),
            username = "Test User",
            issuer = "handbook",
            issuedDateTime = LocalDateTime.now(),
            notBeforeDateTime = LocalDateTime.now(),
            expireDateTime = LocalDateTime.now().plusHours(1),
            token = "old-token",
            sub = userId.toString(),
        )
        every { userRepository.findUserById(userId) } returns Mono.just(user)
        every { tokenFactory.publish(user) } returns "refreshed-jwt-token"

        When("validateRefreshToken을 호출하면") {
            val result = publisher.validateRefreshToken(authentication)

            Then("sub 클레임의 UUID 로 사용자를 조회하여 새 JWT 토큰이 반환된다") {
                StepVerifier.create(result)
                    .expectNext("refreshed-jwt-token")
                    .verifyComplete()
                verify { userRepository.findUserById(userId) }
            }
        }
    }

    Given("레거시 토큰(sub 없음, jti 에 사용자 UUID)으로 갱신할 때") {
        val userId = UUID.randomUUID()
        val user = User(
            id = userId,
            provider = "google",
            account = "oauth-id-123",
            name = "Test User",
            roles = mutableListOf(SystemRole.USER),
        )
        val authentication = UserAuthentication(
            id = userId.toString(), // jti 에 사용자 UUID 가 심어진 레거시 케이스
            username = "Test User",
            issuer = "handbook",
            issuedDateTime = LocalDateTime.now(),
            notBeforeDateTime = LocalDateTime.now(),
            expireDateTime = LocalDateTime.now().plusHours(1),
            token = "legacy-token",
            sub = null,
        )
        every { userRepository.findUserById(userId) } returns Mono.just(user)
        every { tokenFactory.publish(user) } returns "refreshed-legacy-token"

        When("validateRefreshToken을 호출하면") {
            val result = publisher.validateRefreshToken(authentication)

            Then("jti 로 폴백하여 새 토큰을 발급한다 (backward compat)") {
                StepVerifier.create(result)
                    .expectNext("refreshed-legacy-token")
                    .verifyComplete()
            }
        }
    }
    Given("존재하지 않는 사용자 ID로 토큰을 갱신할 때") {
        val nonExistentId = UUID.randomUUID()
        val authentication = UserAuthentication(
            id = nonExistentId.toString(),
            username = "Ghost User",
            issuer = "handbook",
            issuedDateTime = LocalDateTime.now(),
            notBeforeDateTime = LocalDateTime.now(),
            expireDateTime = LocalDateTime.now().plusHours(1),
            token = "expired-token",
        )
        every { userRepository.findUserById(nonExistentId) } returns Mono.empty()

        When("validateRefreshToken을 호출하면") {
            val result = publisher.validateRefreshToken(authentication)

            Then("빈 Mono가 반환된다") {
                StepVerifier.create(result)
                    .verifyComplete()
            }
        }
    }

    Given("User ID가 누락된 인증 객체로 토큰을 갱신할 때") {
        val authentication = UserAuthentication(
            id = null,
            username = "No ID User",
            issuer = "handbook",
            issuedDateTime = LocalDateTime.now(),
            notBeforeDateTime = LocalDateTime.now(),
            expireDateTime = LocalDateTime.now().plusHours(1),
            token = "some-token",
        )

        When("validateRefreshToken을 호출하면") {
            val result = publisher.validateRefreshToken(authentication)

            Then("IllegalArgumentException 에러가 발생한다") {
                StepVerifier.create(result)
                    .expectErrorMatches { it is IllegalArgumentException && it.message == "User ID is missing" }
                    .verify()
            }
        }
    }
})
