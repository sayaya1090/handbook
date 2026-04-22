package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Workspace
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

class WorkspaceSearchServiceTest : DescribeSpec({
    val repo = mockk<WorkspaceReadRepository>()
    val service = WorkspaceSearchService(repo)

    describe("WorkspaceSearchService") {
        it("list: 모든 워크스페이스를 반환한다") {
            val workspace = Workspace(UUID.randomUUID(), "test", null)
            every { repo.findAll() } returns Flux.just(workspace)
            
            service.list().collectList().block() shouldBe listOf(workspace)
        }
        it("listForUser: 사용자가 속한 워크스페이스를 반환한다") {
            val sub = UUID.randomUUID()
            val workspace = Workspace(UUID.randomUUID(), "test", null)
            every { repo.findByUserSub(sub) } returns Flux.just(workspace)
            
            service.listForUser(sub).collectList().block() shouldBe listOf(workspace)
        }
        it("findById: ID로 워크스페이스를 조회한다") {
            val id = UUID.randomUUID()
            val workspace = Workspace(id, "test", null)
            every { repo.findById(id) } returns Mono.just(workspace)
            
            service.findById(id).block() shouldBe workspace
        }
    }
})
