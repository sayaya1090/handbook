package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.User
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.oauth2.core.user.OAuth2User
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockKExtension::class)
internal class TokenPublisherTest: BehaviorSpec({
    Given("PublishToken 인스턴스 생성") {
        val publisher = TokenPublisher(factory = tokenFactory, userRepository = userRepo)
        val now = LocalDateTime.now()
        When("존재하는 사용자 접근 시") {
            val size = users.size
            val token = publisher.publish(PROVIDER, user1Principal).block()
            Then("사용자는 추가되지 않는다") { users.size shouldBe size }
            Then("토큰이 발급된다") { token shouldNotBe null }
            Then("로그인 기록이 남는다") {
                val newUser = users.filter { it.key == user1.id }.values.first()
                newUser.lastLoginDateTime shouldBeGreaterThanOrEqualTo now
            }
        }
        When("존재하지 않는 사용자 접근 시") {
            val principal = mockkClass(OAuth2User::class)
            principal.mock("anonymous")
            val size = users.size
            val token = publisher.publish(PROVIDER, principal).block()
            Then("새로운 사용자가 생성된다") { users.size shouldBe size+1 }
            Then("토큰이 발급된다") { token shouldNotBe null }
            Then("로그인 기록이 남는다") {
                val newUser = users.filter { it.key != user1.id }.values.first()
                newUser.lastLoginDateTime shouldBeGreaterThanOrEqualTo now
            }
        }
    }
}) {
    companion object {
        private const val PROVIDER = "any provider"
        private val user1 = User(id=UUID.randomUUID()).apply {
            lastLoginDateTime = LocalDateTime.of(1900, 1, 1, 0, 0, 0)
        }
        private val user1Principal = mockkClass(OAuth2User::class).mock("user1")
        private val users =  mutableMapOf(user1.id to user1)

        private val tokenFactory = mockkClass(TokenFactory::class, relaxed = true).apply {
            every { publish(any()) }.returns("token")
        }
        private val userRepo = mockkClass(UserRepository::class, relaxed = true).apply {
            every { findById(ofType(UUID::class)) }.answers { answer ->
                Mono.justOrEmpty(users[answer.invocation.args[0] as UUID])
            }
            every { findUserByProviderAndAccount(PROVIDER, "user1") }.returns(Mono.just(user1))
            every { create(any()) }.answers{ answer ->
                val user = answer.invocation.args[0] as User
                users[user.id] = user
                Mono.just(user)
            }
            every { updateLastLoginDateTime(ofType(UUID::class), ofType(LocalDateTime::class)) }.answers { answer ->
                val user = users[answer.invocation.args[0] as UUID]?.let {
                    it.lastLoginDateTime = answer.invocation.args[1] as LocalDateTime
                }
                if(user!=null) Mono.empty()
                else Mono.error(IllegalStateException())
            }
        }
        private fun OAuth2User.mock(name: String): OAuth2User = apply {
            every { getName() }.returns(name)
            every { attributes }.returns(mapOf<String, Any>())
        }
    }
}