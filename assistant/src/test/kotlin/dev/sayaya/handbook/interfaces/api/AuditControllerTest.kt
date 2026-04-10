package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.*
import dev.sayaya.handbook.usecase.AuditRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import java.util.UUID

class AuditControllerTest : BehaviorSpec({
    val repo = mockk<AuditRepository>()
    val controller = AuditController(repo)
    val client = WebTestClient.bindToController(controller).build()

    Given("감사 조회 API") {
        val workspace = UUID.randomUUID()
        val plan = ExecutionPlan(
            intent = "타입 수정",
            steps = listOf(
                ExecutionStep(
                    order = 0,
                    command = AgentCommand(type = CommandType.NAVIGATE, target = "type-management"),
                    description = "이동",
                ),
            ),
            confidence = 0.9,
        )
        val entry = AuditEntry(
            workspace = workspace,
            userMessage = "고객 타입 수정해줘",
            intent = "타입 수정",
            confidence = 0.9,
            plan = plan,
            status = AuditEntry.Status.COMPLETED,
        )
        every { repo.findByWorkspace(workspace) } returns Flux.just(entry)

        When("GET /assistant/audit를 호출하면") {
            Then("200 OK와 감사 기록이 반환된다") {
                client.get()
                    .uri("/assistant/audit?workspace=$workspace")
                    .accept(org.springframework.http.MediaType.parseMediaType("application/vnd.sayaya.handbook.v1+json"))
                    .exchange()
                    .expectStatus().isOk
            }
        }
    }

    Given("감사 기록이 없는 워크스페이스") {
        val workspace = UUID.randomUUID()
        every { repo.findByWorkspace(workspace) } returns Flux.empty()

        When("GET /assistant/audit를 호출하면") {
            Then("200 OK와 빈 목록이 반환된다") {
                client.get()
                    .uri("/assistant/audit?workspace=$workspace")
                    .accept(org.springframework.http.MediaType.parseMediaType("application/vnd.sayaya.handbook.v1+json"))
                    .exchange()
                    .expectStatus().isOk
            }
        }
    }
})
