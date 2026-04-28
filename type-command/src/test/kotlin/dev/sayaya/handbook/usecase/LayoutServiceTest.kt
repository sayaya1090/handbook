package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.TypeLayout
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant
import java.util.*

class LayoutServiceTest : BehaviorSpec({
    val repo = mockk<LayoutRepository>()
    val service = LayoutService(repo)
    val workspace = UUID.randomUUID()

    // UC-PT4: 레이아웃 기간 목록 조회
    Given("워크스페이스 레이아웃 조회 요청이 주어졌을 때") {
        val layout1 = TypeLayout(
            id = UUID.randomUUID(),
            workspace = workspace,
            effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            expireDateTime = Instant.parse("2026-06-30T23:59:59Z"),
            positions = mapOf("customer" to TypeLayout.Position(0, 0, 200, 100)),
        )
        val layout2 = TypeLayout(
            id = UUID.randomUUID(),
            workspace = workspace,
            effectDateTime = Instant.parse("2026-07-01T00:00:00Z"),
            expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
            positions = emptyMap(),
        )
        every { repo.findByWorkspace(workspace) } returns Flux.just(layout1, layout2)

        When("findByWorkspace를 호출하면") {
            val result = service.findByWorkspace(workspace)

            Then("레이아웃 목록이 반환된다") {
                StepVerifier.create(result)
                    .assertNext { it.id shouldBe layout1.id }
                    .assertNext { it.id shouldBe layout2.id }
                    .verifyComplete()
            }
        }
    }

    // UC-PT5: 레이아웃 저장
    Given("레이아웃 저장 요청이 주어졌을 때") {
        val layout = TypeLayout(
            id = UUID.randomUUID(),
            workspace = workspace,
            effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
            positions = mapOf(
                "customer" to TypeLayout.Position(100, 200, 200, 150),
                "order" to TypeLayout.Position(400, 200, 200, 150),
            ),
        )
        every { repo.save(workspace, layout) } returns Mono.just(layout)

        When("save를 호출하면") {
            val result = service.save(workspace, layout)

            Then("저장된 레이아웃이 반환된다") {
                StepVerifier.create(result)
                    .assertNext {
                        it.id shouldBe layout.id
                        it.positions.size shouldBe 2
                    }
                    .verifyComplete()
            }
        }
    }
})
