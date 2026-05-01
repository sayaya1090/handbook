package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Group
import dev.sayaya.handbook.usecase.GroupService
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.util.*

class GroupControllerTest : BehaviorSpec({
    val service = mockk<GroupService>()
    val controller = GroupController(service)
    val client = WebTestClient.bindToController(controller).build()

    val workspaceId = UUID.randomUUID()
    val groupId = UUID.randomUUID()
    val userId = UUID.randomUUID()
    val group = Group.create(groupId.toString(), workspaceId.toString(), "Test Group", "Description")

    Given("그룹 생성 API") {
        When("유효한 요청으로 POST /workspaces/{ws}/groups 를 호출하면") {
            every { service.createGroup(workspaceId, "Test Group", "Description") } returns Mono.just(group)
            
            val response = client.post()
                .uri("/workspaces/$workspaceId/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapOf("name" to "Test Group", "description" to "Description"))
                .exchange()

            Then("201 Created 를 반환하고 생성된 그룹을 응답한다") {
                response.expectStatus().isCreated
                response.expectBody()
                    .jsonPath("$.id").isEqualTo(groupId.toString())
                    .jsonPath("$.name").isEqualTo("Test Group")
            }
        }
    }

    Given("그룹 삭제 API") {
        When("DELETE /workspaces/{ws}/groups/{gid} 를 호출하면") {
            every { service.deleteGroup(workspaceId, groupId) } returns Mono.empty()
            
            val response = client.delete()
                .uri("/workspaces/$workspaceId/groups/$groupId")
                .exchange()

            Then("204 No Content 를 반환한다") {
                response.expectStatus().isNoContent
                verify(exactly = 1) { service.deleteGroup(workspaceId, groupId) }
            }
        }
    }

    Given("멤버 추가 API") {
        When("POST /workspaces/{ws}/groups/{gid}/members/{uid} 를 호출하면") {
            every { service.addMember(workspaceId, groupId, userId) } returns Mono.empty()
            
            val response = client.post()
                .uri("/workspaces/$workspaceId/groups/$groupId/members/$userId")
                .exchange()

            Then("204 No Content 를 반환한다") {
                response.expectStatus().isNoContent
                verify(exactly = 1) { service.addMember(workspaceId, groupId, userId) }
            }
        }
    }

    Given("멤버 삭제 API") {
        When("DELETE /workspaces/{ws}/groups/{gid}/members/{uid} 를 호출하면") {
            every { service.removeMember(workspaceId, groupId, userId) } returns Mono.empty()
            
            val response = client.delete()
                .uri("/workspaces/$workspaceId/groups/$groupId/members/$userId")
                .exchange()

            Then("204 No Content 를 반환한다") {
                response.expectStatus().isNoContent
                verify(exactly = 1) { service.removeMember(workspaceId, groupId, userId) }
            }
        }
    }
})
