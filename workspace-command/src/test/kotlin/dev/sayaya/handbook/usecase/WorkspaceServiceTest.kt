package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Group
import dev.sayaya.handbook.domain.Workspace
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.security.Principal
import java.util.*

class WorkspaceServiceTest : BehaviorSpec({
    val workspaceRepo = mockk<WorkspaceRepository>()
    val groupRepo = mockk<GroupRepository>()
    val webhookService = mockk<WebhookService>()
    val eventPublisher = mockk<WorkspaceEventPublisher>()
    // 테스트에서는 TransactionalOperator 를 no-op 으로 사용 — delegate 체인만 검증
    val tx = mockk<TransactionalOperator>()
    every { tx.transactional(any<Mono<Void>>()) } answers { firstArg() }
    val service = WorkspaceService(workspaceRepo, groupRepo, webhookService, eventPublisher, tx)

    Given("워크스페이스 생성 요청이 주어졌을 때") {
        val principal = mockk<Principal>()
        val creator = UUID.randomUUID()
        val name = "MyWorkspace"
        val description = "테스트 워크스페이스"

        every { workspaceRepo.save(any(), creator) } answers {
            Mono.just(firstArg())
        }
        every { groupRepo.createAndAssign(any(), principal, "Admin", null) } returns Mono.just(
            Group(UUID.randomUUID(), UUID.randomUUID(), "Admin", null)
        )
        every { groupRepo.save(any()) } answers { Mono.just(firstArg()) }
        every { eventPublisher.publishCreated(any()) } returns Mono.empty()

        When("create를 호출하면") {
            val result = service.create(creator, principal, name, description)

            Then("워크스페이스가 생성된다") {
                StepVerifier.create(result)
                    .assertNext {
                        it.name shouldBe name
                        it.description shouldBe description
                    }
                    .verifyComplete()
            }
            Then("워크스페이스 저장 시 creator UUID 가 함께 전달된다 — created_by 감사 회귀 방지") {
                verify(exactly = 1) { workspaceRepo.save(any(), creator) }
            }
            Then("Admin 및 Member 그룹이 생성된다") {
                verify { groupRepo.createAndAssign(any(), principal, "Admin", null) }
                verify { groupRepo.save(match { it.name == "Member" }) }
            }
            Then("WORKSPACE_CREATED 이벤트가 발행된다") {
                verify { eventPublisher.publishCreated(any()) }
            }
        }
    }

    Given("워크스페이스 수정 요청이 주어졌을 때") {
        val workspace = Workspace(UUID.randomUUID(), "UpdatedName", "수정된 설명")
        val modifier = UUID.randomUUID()
        every { workspaceRepo.update(workspace, modifier) } returns Mono.just(workspace)

        When("update를 호출하면") {
            val result = service.update(workspace, modifier)

            Then("수정된 워크스페이스가 반환된다") {
                StepVerifier.create(result)
                    .assertNext { it.name shouldBe "UpdatedName" }
                    .verifyComplete()
            }
            Then("modifier UUID 가 repo.update 에 전달된다 — last_modified_by 감사 회귀 방지") {
                verify(exactly = 1) { workspaceRepo.update(workspace, modifier) }
            }
        }
    }

    Given("워크스페이스 삭제 요청이 주어졌을 때") {
        val id = UUID.randomUUID()
        every { webhookService.deleteByWorkspace(id) } returns Mono.empty()
        every { groupRepo.deleteByWorkspace(id) } returns Mono.empty()
        every { workspaceRepo.delete(id) } returns Mono.empty()
        every { eventPublisher.publishDeleted(id) } returns Mono.empty()

        When("delete를 호출하면") {
            val result = service.delete(id)

            Then("성공적으로 완료된다") {
                StepVerifier.create(result)
                    .verifyComplete()
            }
            Then("cascade 순서대로 웹훅 → 그룹 → 워크스페이스가 삭제된다") {
                verifyOrder {
                    webhookService.deleteByWorkspace(id)
                    groupRepo.deleteByWorkspace(id)
                    workspaceRepo.delete(id)
                }
            }
            Then("트랜잭션으로 묶인 뒤 WORKSPACE_DELETED 이벤트가 발행된다") {
                verify(exactly = 1) { tx.transactional(any<Mono<Void>>()) }
                verify(exactly = 1) { eventPublisher.publishDeleted(id) }
            }
        }
    }

    Given("워크스페이스 참여 요청이 주어졌을 때") {
        val workspaceId = UUID.randomUUID()
        val principal = mockk<Principal>()
        every { groupRepo.addMember(workspaceId, principal) } returns Mono.empty()

        When("join을 호출하면") {
            val result = service.join(workspaceId, principal)

            Then("성공적으로 완료된다") {
                StepVerifier.create(result)
                    .verifyComplete()
            }
            Then("그룹에 멤버가 추가된다") {
                verify { groupRepo.addMember(workspaceId, principal) }
            }
        }
    }
})
