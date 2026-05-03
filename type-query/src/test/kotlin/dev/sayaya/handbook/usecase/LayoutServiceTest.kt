package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.TypeLayout
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.time.Instant
import java.util.*

class LayoutServiceTest : BehaviorSpec({
    val repo = mockk<LayoutSearchRepository>()
    val service = LayoutSearchService(repo)
    val workspace = UUID.randomUUID()

    Given("워크스페이스 레이아웃 조회 요청이 주어졌을 때") {
        val layout = TypeLayout()
            .id(UUID.randomUUID().toString())
            .workspace(workspace.toString())
            .effectDateTime(Instant.parse("2026-01-01T00:00:00Z").toEpochMilli().toDouble())
            .expireDateTime(Instant.parse("2026-12-31T23:59:59Z").toEpochMilli().toDouble())
        every { repo.findByWorkspace(workspace) } returns Flux.just(layout)

        When("findByWorkspace를 호출하면") {
            val result = service.findByWorkspace(workspace)

            Then("레이아웃 목록이 반환된다") {
                StepVerifier.create(result)
                    .assertNext { it.id() shouldBe layout.id() }
                    .verifyComplete()
            }
        }
    }
})