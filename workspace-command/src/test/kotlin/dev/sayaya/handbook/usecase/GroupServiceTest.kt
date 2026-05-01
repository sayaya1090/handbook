package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Group
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import reactor.core.publisher.Mono
import java.util.*

class GroupServiceTest : BehaviorSpec({
    val groupRepo = mockk<GroupRepository>()
    val service = GroupService(groupRepo)

    val workspaceId = UUID.randomUUID()
    val groupId = UUID.randomUUID()
    val userId = UUID.randomUUID()
    val group = Group.create(groupId.toString(), workspaceId.toString(), "Test Group", "Description")

    Given("그룹 생성 로직") {
        When("새 그룹 생성을 요청하면") {
            every { groupRepo.save(any()) } returns Mono.just(group)
            val result = service.createGroup(workspaceId, "Test Group", "Description").block()

            Then("리포지토리에 저장하고 생성된 그룹을 반환한다") {
                result shouldBe group
                verify(exactly = 1) { groupRepo.save(any()) }
            }
        }
    }

    Given("그룹 삭제 로직") {
        When("그룹 삭제를 요청하면") {
            every { groupRepo.delete(workspaceId, groupId) } returns Mono.empty()
            service.deleteGroup(workspaceId, groupId).block()

            Then("리포지토리의 delete 를 호출한다") {
                verify(exactly = 1) { groupRepo.delete(workspaceId, groupId) }
            }
        }
    }

    Given("멤버 배정 로직") {
        When("멤버 추가를 요청하면") {
            every { groupRepo.addMember(groupId, userId) } returns Mono.empty()
            service.addMember(workspaceId, groupId, userId).block()

            Then("리포지토리의 addMember 를 호출한다") {
                verify(exactly = 1) { groupRepo.addMember(groupId, userId) }
            }
        }

        When("멤버 삭제를 요청하면") {
            every { groupRepo.removeMember(groupId, userId) } returns Mono.empty()
            service.removeMember(workspaceId, groupId, userId).block()

            Then("리포지토리의 removeMember 를 호출한다") {
                verify(exactly = 1) { groupRepo.removeMember(groupId, userId) }
            }
        }
    }
})
