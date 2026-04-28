package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Workspace
import dev.sayaya.handbook.interfaces.authentication.UserAuthentication
import dev.sayaya.handbook.usecase.WorkspaceSearchService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.UUID

class WorkspaceControllerTest : BehaviorSpec({
    val service = mockk<WorkspaceSearchService>()
    val controller = WorkspaceController(service)
    val client = WebTestClient.bindToController(controller).build()

    fun principalWith(sub: String? = null, id: String? = null) = UserAuthentication(
        id = id,
        username = "Test User",
        issuer = "test",
        issuedDateTime = LocalDateTime.now(),
        notBeforeDateTime = LocalDateTime.now(),
        expireDateTime = LocalDateTime.now().plusHours(1),
        token = "dummy.jwt.token",
        sub = sub,
    )

    Given("WorkspaceController list") {
        val userUuid = UUID.randomUUID()
        val ws = Workspace(UUID.randomUUID(), "alpha", "desc")
        
        When("sub와 id가 모두 없는 경우") {
            val auth = principalWith(sub = null, id = null)
            val result = controller.list(auth).collectList().block()!!
            Then("서비스 호출 없이 빈 Flux 를 반환한다") {
                result shouldBe emptyList()
                verify(exactly = 0) { service.listForUser(any()) }
            }
        }
        When("sub는 없지만 id(UUID)가 있는 경우 (Phase 1a 폴백)") {
            val auth = principalWith(sub = null, id = userUuid.toString())
            every { service.listForUser(userUuid) } returns Flux.just(ws)
            val result = controller.list(auth).collectList().block()!!
            Then("id를 사용하여 서비스를 호출한다") {
                result shouldContainExactly listOf(ws)
                verify { service.listForUser(userUuid) }
            }
        }
        When("sub가 UUID 형식이 아닌 문자열인 경우") {
            val auth = principalWith(sub = "not-a-uuid", id = userUuid.toString())
            val result = controller.list(auth).collectList().block()!!
            Then("UUID 변환 실패로 빈 Flux 를 반환한다") {
                result shouldBe emptyList()
            }
        }
        When("sub가 null이고 id가 UUID 형식이 아닌 경우") {
            val auth = principalWith(sub = null, id = "not-a-uuid")
            val result = controller.list(auth).collectList().block()!!
            Then("id 변환 실패로 빈 Flux 를 반환한다") {
                result shouldBe emptyList()
            }
        }
        When("정상적인 sub(UUID)가 있는 경우") {
            val auth = principalWith(sub = userUuid.toString(), id = null)
            every { service.listForUser(userUuid) } returns Flux.just(ws)
            val result = controller.list(auth).collectList().block()!!
            Then("sub를 최우선으로 사용하여 서비스를 호출한다") {
                result shouldContainExactly listOf(ws)
                verify { service.listForUser(userUuid) }
            }
        }
    }

    Given("WorkspaceController get") {
        val id = UUID.randomUUID()
        val ws = Workspace(id, "alpha", "desc")
        When("ID로 단건 조회 시") {
            every { service.findById(id) } returns Mono.just(ws)
            Then("200 OK와 워크스페이스를 반환한다") {
                client.get().uri("/workspaces/$id")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(Workspace::class.java).isEqualTo(ws)
            }
        }
    }
})
