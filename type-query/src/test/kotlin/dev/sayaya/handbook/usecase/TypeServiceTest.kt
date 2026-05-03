package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Type
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.time.Instant
import java.util.*

class TypeServiceTest : BehaviorSpec({
    val repo = mockk<TypeSearchRepository>()
    val service = TypeSearchService(repo)
    val workspace = UUID.randomUUID()

    val type = Type.create(
        "customer",
        "1.0",
        Instant.parse("2026-01-01T00:00:00Z").toEpochMilli().toDouble(),
        Instant.parse("2026-12-31T23:59:59Z").toEpochMilli().toDouble()
    ).description("고객 타입").primitive(false)

    Given("기간이 지정된 타입 조회 요청이 주어졌을 때") {
        val effectDateTime = Instant.parse("2026-01-01T00:00:00Z")
        val expireDateTime = Instant.parse("2026-12-31T23:59:59Z")
        every { repo.findByRange(workspace, effectDateTime, expireDateTime) } returns Flux.just(type)

        When("findByRange를 effectDateTime과 expireDateTime으로 호출하면") {
            val result = service.findByRange(workspace, effectDateTime, expireDateTime)

            Then("기간에 해당하는 타입이 반환된다") {
                StepVerifier.create(result)
                    .assertNext { it.id() shouldBe "customer" }
                    .verifyComplete()
            }
        }
    }

    Given("effectDateTime만 지정된 조회 요청이 주어졌을 때") {
        val effectDateTime = Instant.parse("2026-06-15T00:00:00Z")
        every { repo.findByRange(workspace, effectDateTime, effectDateTime) } returns Flux.just(type)

        When("findByRange를 effectDateTime만 지정하고 expireDateTime은 null로 호출하면") {
            val result = service.findByRange(workspace, effectDateTime, null)

            Then("effectDateTime을 expireDateTime으로 사용하여 조회한다") {
                StepVerifier.create(result)
                    .assertNext { it.id() shouldBe "customer" }
                    .verifyComplete()
                verify { repo.findByRange(workspace, effectDateTime, effectDateTime) }
            }
        }
    }

    Given("기간이 지정되지 않은 조회 요청이 주어졌을 때") {
        every { repo.findAll(workspace) } returns Flux.just(type)

        When("findByRange를 effectDateTime null로 호출하면") {
            val result = service.findByRange(workspace, null, null)

            Then("전체 타입 목록이 반환된다") {
                StepVerifier.create(result)
                    .assertNext { it.id() shouldBe "customer" }
                    .verifyComplete()
                verify { repo.findAll(workspace) }
            }
        }
    }

    Given("타입 버전 조회 요청이 주어졌을 때") {
        val typeId = "customer"
        every { repo.findVersions(workspace, typeId) } returns Flux.just(type)

        When("findVersions를 호출하면") {
            val result = service.findVersions(workspace, typeId)

            Then("해당 타입의 모든 버전이 반환된다") {
                StepVerifier.create(result)
                    .assertNext { it.id() shouldBe "customer" }
                    .verifyComplete()
            }
        }
    }
})
