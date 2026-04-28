package dev.sayaya.handbook.usecase

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.time.Instant
import java.util.*

class StatsServiceTest : BehaviorSpec({
    val repo = mockk<StatsRepository>()
    val service = StatsService(repo)
    val workspace = UUID.randomUUID()

    Given("타임라인 통계 조회") {
        val from = Instant.parse("2026-01-01T00:00:00Z")
        val to = Instant.parse("2026-03-31T23:59:59Z")

        When("유효한 기간으로 조회하면") {
            val entries = listOf(
                TimelineEntry("2026-01-01", 10, 2, 1),
                TimelineEntry("2026-02-01", 15, 0, 3),
                TimelineEntry("2026-03-01", 8, 1, 0),
            )
            every { repo.timeline(workspace, from, to, 1) } returns Flux.fromIterable(entries)

            Then("타임라인 엔트리 목록이 반환된다") {
                StepVerifier.create(service.timeline(workspace, from, to))
                    .assertNext { it.date shouldBe "2026-01-01"; it.documentCount shouldBe 10 }
                    .assertNext { it.date shouldBe "2026-02-01"; it.documentCount shouldBe 15 }
                    .assertNext { it.date shouldBe "2026-03-01"; it.documentCount shouldBe 8 }
                    .verifyComplete()
            }
        }

        When("결과가 없는 기간으로 조회하면") {
            val emptyFrom = Instant.parse("2030-01-01T00:00:00Z")
            val emptyTo = Instant.parse("2030-12-31T23:59:59Z")
            every { repo.timeline(workspace, emptyFrom, emptyTo, 1) } returns Flux.empty()

            Then("빈 결과가 반환된다") {
                StepVerifier.create(service.timeline(workspace, emptyFrom, emptyTo))
                    .verifyComplete()
            }
        }
    }

    Given("타입별 분포 조회") {
        When("문서가 있는 워크스페이스를 조회하면") {
            val entries = listOf(
                DistributionEntry("customer", 25),
                DistributionEntry("invoice", 42),
            )
            every { repo.distribution(workspace) } returns Flux.fromIterable(entries)

            Then("분포 엔트리 목록이 반환된다") {
                StepVerifier.create(service.distribution(workspace))
                    .assertNext { it.type shouldBe "customer"; it.count shouldBe 25 }
                    .assertNext { it.type shouldBe "invoice"; it.count shouldBe 42 }
                    .verifyComplete()
            }
        }

        When("문서가 없는 워크스페이스를 조회하면") {
            val emptyWs = UUID.randomUUID()
            every { repo.distribution(emptyWs) } returns Flux.empty()

            Then("빈 결과가 반환된다") {
                StepVerifier.create(service.distribution(emptyWs))
                    .verifyComplete()
            }
        }
    }
})
