package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.domain.TypePatch
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant
import java.util.*

class TypeServiceTest : BehaviorSpec({
    val repo = mockk<TypeRepository>()
    val publisher = mockk<TypeEventPublisher>(relaxed = true)
    val service = TypeService(repo, publisher)
    val workspace = UUID.randomUUID()

    // UC-PT1: 타입 조회 (기간별)
    Given("기간별 타입 조회 요청이 주어졌을 때") {
        val effectDateTime = Instant.parse("2026-01-01T00:00:00Z")
        val expireDateTime = Instant.parse("2026-12-31T23:59:59Z")
        val type1 = Type(
            id = "customer",
            version = "1.0",
            effectDateTime = effectDateTime,
            expireDateTime = expireDateTime,
            description = "고객 타입",
            primitive = false,
        )
        val type2 = Type(
            id = "order",
            version = "1.0",
            effectDateTime = effectDateTime,
            expireDateTime = expireDateTime,
            description = "주문 타입",
            primitive = false,
        )
        every { repo.findByWorkspaceAndPeriod(workspace, effectDateTime, expireDateTime) } returns Flux.just(type1, type2)

        When("findByPeriod를 호출하면") {
            val result = service.findByPeriod(workspace, effectDateTime, expireDateTime)

            Then("해당 기간의 타입 목록이 반환된다") {
                StepVerifier.create(result)
                    .assertNext { it.id shouldBe "customer" }
                    .assertNext { it.id shouldBe "order" }
                    .verifyComplete()
            }
        }
    }

    // UC-PT2: 타입 저장
    Given("타입 저장 요청이 주어졌을 때") {
        val type = Type(
            id = "product",
            version = "1.0",
            effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
            description = "상품 타입",
            primitive = false,
        )
        every { repo.save(workspace, listOf(type)) } returns Flux.just(type)

        When("save를 호출하면") {
            val result = service.save(workspace, listOf(type))

            Then("저장된 타입이 반환된다") {
                StepVerifier.create(result)
                    .assertNext { it.id shouldBe "product" }
                    .verifyComplete()
            }
            Then("TYPE_CREATED 이벤트가 발행된다") {
                verify { publisher.publishCreated(workspace, type) }
            }
        }
    }

    Given("타입 패치 요청이 주어졌을 때") {
        val type = Type(
            id = "customer",
            version = "1.0",
            effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
            description = "고객 타입",
            primitive = false,
        )
        val patch = TypePatch(id = "customer", version = "1.0", rev = 1)
        every { repo.patch(workspace, listOf(patch)) } returns Flux.just(type)

        When("patch를 호출하면") {
            val result = service.patch(workspace, listOf(patch))

            Then("패치된 타입이 반환된다") {
                StepVerifier.create(result)
                    .assertNext { it.id shouldBe "customer" }
                    .verifyComplete()
            }
            Then("TYPE_CREATED 이벤트가 발행된다") {
                verify { publisher.publishCreated(workspace, type) }
            }
        }
    }

    // UC-PT3: 타입 삭제
    Given("타입 삭제 요청이 주어졌을 때") {
        val type = Type(
            id = "obsolete",
            version = "1.0",
            effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
            description = "삭제 대상",
            primitive = false,
        )
        every { repo.delete(workspace, listOf(type)) } returns Mono.empty()

        When("delete를 호출하면") {
            val result = service.delete(workspace, listOf(type))

            Then("성공적으로 완료된다") {
                StepVerifier.create(result)
                    .verifyComplete()
            }
            Then("TYPE_DELETED 이벤트가 발행된다") {
                verify { publisher.publishDeleted(workspace, type) }
            }
        }
    }
})
