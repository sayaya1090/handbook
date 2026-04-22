package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.SystemRole
import dev.sayaya.handbook.domain.User
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.time.LocalDateTime
import java.util.*

class R2dbcUserRepositoryDelegateTest : DescribeSpec({
    val repo = mockk<R2dbcUserRepository>()
    val delegate = R2dbcUserRepositoryDelegate(repo)

    describe("R2dbcUserRepositoryDelegate") {
        it("findUserById: ID로 사용자를 조회하여 도메인 객체로 변환한다") {
            val id = UUID.randomUUID()
            val entity = R2dbcUserEntity(id, "google", "123", "Alice")
            every { repo.findById(id) } returns entity.toMono()

            val result = delegate.findUserById(id).block()

            result?.id shouldBe id
            result?.name shouldBe "Alice"
            result?.roles shouldBe mutableListOf(SystemRole.USER)
        }
        it("findUserByProviderAndAccount: 제공자와 계정으로 사용자를 조회한다") {
            val id = UUID.randomUUID()
            val entity = R2dbcUserEntity(id, "google", "123", "Alice")
            every { repo.findByProviderAndAccount("google", "123") } returns entity.toMono()

            val result = delegate.findUserByProviderAndAccount("google", "123").block()

            result?.id shouldBe id
            result?.provider shouldBe "google"
            result?.account shouldBe "123"
        }
        it("create: 도메인 객체를 엔티티로 변환하여 저장한다 (new=true 설정)") {
            val id = UUID.randomUUID()
            val user = User(id, "google", "123", "Alice", mutableListOf(SystemRole.USER))
            val entity = R2dbcUserEntity(id, "google", "123", "Alice")
            every { repo.save(any()) } returns entity.toMono()

            delegate.create(user).block()

            verify { repo.save(match { it.id == id && it.new }) }
        }
        it("updateLastLoginDateTime: 마지막 로그인 시각을 업데이트한다") {
            val id = UUID.randomUUID()
            val now = LocalDateTime.now()
            every { repo.updateLastLoginDateTimeById(id, now) } returns Mono.just(1)

            delegate.updateLastLoginDateTime(id, now).block()

            verify { repo.updateLastLoginDateTimeById(id, now) }
        }
    }
})
