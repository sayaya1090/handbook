package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.*
import dev.sayaya.handbook.usecase.AssistantService
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.util.UUID

class AssistantControllerTest : BehaviorSpec({
    val service = mockk<AssistantService>()
    val controller = AssistantController(service)
    val client = WebTestClient.bindToController(controller).build()

    Given("자연어 요청 API") {
        val workspace = UUID.randomUUID()
        val plan = ExecutionPlan(
            intent = "타입 정의 수정",
            steps = listOf(
                ExecutionStep(
                    order = 0,
                    command = AgentCommand(type = CommandType.NAVIGATE, target = "type-management"),
                    description = "타입 관리 화면으로 이동",
                ),
            ),
            confidence = 0.95,
        )
        every { service.request(workspace, "고객 타입 정의를 수정해줘") } returns Mono.just(plan)

        When("POST /assistant/request를 호출하면") {
            Then("200 OK와 실행 계획이 반환된다") {
                client.post()
                    .uri("/assistant/request?workspace=$workspace")
                    .header("Content-Type", "application/vnd.sayaya.handbook.v1+json")
                    .bodyValue(mapOf("message" to "고객 타입 정의를 수정해줘"))
                    .exchange()
                    .expectStatus().isOk
            }
        }
    }

    Given("실행 계획 실행 API") {
        val workspace = UUID.randomUUID()
        val plan = ExecutionPlan(
            intent = "이동",
            steps = emptyList(),
            confidence = 0.9,
        )
        every { service.execute(any(), any()) } returns Mono.empty()

        When("POST /assistant/execute를 호출하면") {
            Then("202 Accepted가 반환된다") {
                client.post()
                    .uri("/assistant/execute?workspace=$workspace")
                    .header("Content-Type", "application/vnd.sayaya.handbook.v1+json")
                    .bodyValue(plan)
                    .exchange()
                    .expectStatus().isAccepted
            }
        }
    }

    Given("실행 취소 API") {
        every { service.abort() } returns Mono.empty()

        When("POST /assistant/abort를 호출하면") {
            Then("204 No Content가 반환된다") {
                client.post()
                    .uri("/assistant/abort")
                    .exchange()
                    .expectStatus().isNoContent
            }
        }
    }
})
