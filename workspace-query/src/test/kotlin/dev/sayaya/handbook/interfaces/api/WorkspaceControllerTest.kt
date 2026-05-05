package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Workspace
import dev.sayaya.handbook.interfaces.authentication.UserAuthentication
import dev.sayaya.handbook.usecase.WorkspaceSearchService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.URI
import java.time.LocalDateTime
import java.util.*

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

    afterContainer {
        clearMocks(service)
    }

    Given("WorkspaceController list (UC-05, UC-12)") {
        val userUuid = UUID.randomUUID()
        val ws = Workspace.create(UUID.randomUUID().toString(), "alpha", "desc")
        
        When("인증된 사용자의 워크스페이스가 0개인 경우 (UC-12)") {
            val auth = principalWith(sub = userUuid.toString())
            every { service.listForUser(userUuid) } returns Flux.empty()
            
            val response = controller.list(auth).block()!!
            Then("302 Found 와 함께 온보딩 경로로 리다이렉트된다") {
                response.statusCode shouldBe HttpStatus.FOUND
                response.headers.location shouldBe URI.create("/workspaces/onboarding")
            }
        }
        When("인증된 사용자의 워크스페이스가 1개 이상인 경우 (UC-05)") {
            val auth = principalWith(sub = userUuid.toString())
            every { service.listForUser(userUuid) } returns Flux.just(ws)
            
            val response = controller.list(auth).block()!!
            Then("200 OK 와 함께 워크스페이스 목록을 반환한다") {
                response.statusCode shouldBe HttpStatus.OK
                response.body!! shouldBe listOf(ws)
            }
        }
        When("sub와 id가 모두 없는 경우") {
            val auth = principalWith(sub = null, id = null)
            val response = controller.list(auth).block()!!
            Then("빈 목록을 반환한다") {
                response.statusCode shouldBe HttpStatus.OK
                response.body!! shouldBe emptyList<Workspace>()
                verify(exactly = 0) { service.listForUser(any()) }
            }
        }
        When("sub는 없지만 id(UUID)가 있는 경우 (Phase 1a 폴백)") {
            val auth = principalWith(sub = null, id = userUuid.toString())
            every { service.listForUser(userUuid) } returns Flux.just(ws)
            val response = controller.list(auth).block()!!
            Then("id를 사용하여 서비스를 호출한다") {
                response.statusCode shouldBe HttpStatus.OK
                response.body!! shouldBe listOf(ws)
                verify { service.listForUser(userUuid) }
            }
        }
        When("sub가 UUID 형식이 아닌 문자열인 경우") {
            val auth = principalWith(sub = "not-a-uuid", id = userUuid.toString())
            val response = controller.list(auth).block()!!
            Then("UUID 변환 실패로 빈 목록을 반환한다") {
                response.statusCode shouldBe HttpStatus.OK
                response.body!! shouldBe emptyList<Workspace>()
            }
        }
    }

    Given("WorkspaceController get (UC-85)") {
        val id = UUID.randomUUID()
        val ws = Workspace.create(id.toString(), "alpha", "desc")
        When("ID로 단건 조회 시") {
            every { service.findById(id) } returns Mono.just(ws)
            Then("200 OK와 워크스페이스를 반환한다") {
                client.get().uri("/workspaces/$id")
                    .header("Accept", "application/vnd.sayaya.handbook.v1+json")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(ws.id())
                    .jsonPath("$.name").isEqualTo(ws.name())
                    .jsonPath("$.description").isEqualTo(ws.description())
            }
        }
    }
})
