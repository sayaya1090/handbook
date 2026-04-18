package dev.sayaya.handbook.interfaces.authentication

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import java.time.LocalDateTime

internal class UserAuthenticationTest : DescribeSpec({
    describe("UserAuthentication 객체는") {
        context("생성되었을 때") {
            val now = LocalDateTime.now()
            val auth = UserAuthentication(
                id = "jti-token-abc",
                username = "test-user",
                issuer = "test-issuer",
                issuedDateTime = now,
                notBeforeDateTime = now,
                expireDateTime = now.plusHours(1),
                token = "sample.jwt.token",
                sub = "user-uuid-123",
            )

            it("모든 프로퍼티를 올바르게 가지고 있다") {
                auth.id shouldBe "jti-token-abc"
                auth.sub shouldBe "user-uuid-123"
                auth.username shouldBe "test-user"
                auth.issuer shouldBe "test-issuer"
                auth.issuedDateTime shouldBe now
                auth.notBeforeDateTime shouldBe now
                auth.expireDateTime shouldBe now.plusHours(1)
            }

            it("sub 필드는 기본값이 null 이어서 backward compat 을 지원한다") {
                val legacy = UserAuthentication(
                    id = "jti-only",
                    username = "legacy-user",
                    issuer = "issuer",
                    issuedDateTime = now,
                    notBeforeDateTime = now,
                    expireDateTime = now.plusHours(1),
                    token = "legacy-token",
                )
                legacy.sub shouldBe null
                legacy.id shouldBe "jti-only"
            }

            it("Spring Security의 Principal, Credentials를 올바르게 반환한다") {
                auth.name shouldBe "test-user"
                // principal 은 자기 자신 (컨트롤러에서 @AuthenticationPrincipal UserAuthentication 주입용).
                // equals 는 identity 비교이므로 shouldBe 대신 identity 매처 사용.
                auth.principal shouldBeSameInstanceAs auth
                auth.credentials shouldBe "sample.jwt.token"
            }

            it("초기 인증 상태는 false 이다") {
                auth.isAuthenticated shouldBe false
            }
        }

        context("인증 상태(isAuthenticated)는") {
            val auth = UserAuthentication("id", "user", "issuer", LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "token")

            it("true로 변경할 수 있다") {
                auth.isAuthenticated = true
                auth.isAuthenticated shouldBe true
            }

            it("한번 true가 되면 다시 false로 변경할 수 없다") {
                val authenticatedAuth = UserAuthentication("id", "user", "issuer", LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "token").apply {
                    isAuthenticated = true
                }
                shouldThrow<IllegalArgumentException> {
                    authenticatedAuth.isAuthenticated = false
                }
            }
        }
    }
})
