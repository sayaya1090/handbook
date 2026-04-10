package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.*
import dev.sayaya.handbook.usecase.AssistantService
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
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
                    group = 0,
                    order = 0,
                    command = AgentCommand(type = CommandType.NAVIGATE, target = "type-management"),
                    description = "타입 관리 화면으로 이동",
                ),
            ),
            confidence = 0.95,
        )
        val executionId = UUID.randomUUID()
        val executionRequest = ExecutionRequest(executionId = executionId, plan = plan)
        every { service.request(workspace, "고객 타입 정의를 수정해줘") } returns Mono.just(executionRequest)

        When("POST /assistant/request를 호출하면") {
            Then("200 OK와 ExecutionRequest가 반환된다") {
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
        val executionId = UUID.randomUUID()
        val plan = ExecutionPlan(
            intent = "이동",
            steps = emptyList(),
            confidence = 0.9,
        )
        every { service.execute(workspace, executionId, plan) } returns Mono.empty()

        When("POST /assistant/execute를 호출하면") {
            Then("202 Accepted가 반환된다") {
                client.post()
                    .uri("/assistant/execute?workspace=$workspace&executionId=$executionId")
                    .header("Content-Type", "application/vnd.sayaya.handbook.v1+json")
                    .bodyValue(plan)
                    .exchange()
                    .expectStatus().isAccepted
            }
        }
    }

    Given("실행 취소 API") {
        val executionId = UUID.randomUUID()
        every { service.abort(executionId) } returns Mono.empty()

        When("POST /assistant/abort를 호출하면") {
            Then("204 No Content가 반환된다") {
                client.post()
                    .uri("/assistant/abort?executionId=$executionId")
                    .exchange()
                    .expectStatus().isNoContent
            }
        }
    }

    Given("실행 상태 조회 API (UC-A9)") {
        val workspace = UUID.randomUUID()
        val executionId1 = UUID.randomUUID()
        val executionId2 = UUID.randomUUID()
        val executions = listOf(
            mapOf<String, Any>(
                "executionId" to executionId1,
                "intent" to "타입 정의 수정",
                "status" to "EXECUTING",
                "currentGroup" to 1,
                "totalGroups" to 3,
            ),
            mapOf<String, Any>(
                "executionId" to executionId2,
                "intent" to "문서 검증",
                "status" to "AWAITING_CONFIRM",
                "currentGroup" to 0,
                "totalGroups" to 1,
            ),
        )
        every { service.getExecutions(workspace) } returns Flux.fromIterable(executions)

        When("GET /assistant/executions를 호출하면") {
            Then("200 OK와 실행 상태 목록이 반환된다") {
                client.get()
                    .uri("/assistant/executions?workspace=$workspace")
                    .header("Accept", "application/vnd.sayaya.handbook.v1+json")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .jsonPath("$.length()").isEqualTo(2)
                    .jsonPath("$[0].intent").isEqualTo("타입 정의 수정")
                    .jsonPath("$[0].status").isEqualTo("EXECUTING")
                    .jsonPath("$[1].intent").isEqualTo("문서 검증")
                    .jsonPath("$[1].status").isEqualTo("AWAITING_CONFIRM")
            }
        }
    }

    Given("활성 실행이 없는 워크스페이스에서 실행 상태를 조회할 때 (UC-A9)") {
        val workspace = UUID.randomUUID()
        every { service.getExecutions(workspace) } returns Flux.empty()

        When("GET /assistant/executions를 호출하면") {
            Then("200 OK와 빈 목록이 반환된다") {
                client.get()
                    .uri("/assistant/executions?workspace=$workspace")
                    .header("Accept", "application/vnd.sayaya.handbook.v1+json")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .jsonPath("$.length()").isEqualTo(0)
            }
        }
    }

    Given("아티팩트 조회 API (UC-A10)") {
        val workspace = UUID.randomUUID()
        val executionId = UUID.randomUUID()
        val artifact = Artifact(
            executionId = executionId,
            summary = "고객 타입에 이메일 필드 추가",
            changes = listOf(
                ArtifactChange(type = "NAVIGATE", target = "type-management", description = "타입 관리 화면으로 이동"),
                ArtifactChange(type = "MUTATE", target = "customer.email", description = "이메일 필드 추가"),
            ),
        )
        val auditEntry = AuditEntry(
            id = executionId,
            workspace = workspace,
            userMessage = "고객 타입에 이메일 추가해줘",
            intent = "고객 타입에 이메일 필드 추가",
            confidence = 0.95,
            plan = ExecutionPlan(intent = "고객 타입에 이메일 필드 추가", steps = emptyList(), confidence = 0.95),
            status = AuditEntry.Status.COMPLETED,
            artifact = artifact,
        )
        every { service.getArtifacts(workspace) } returns Flux.just(auditEntry)

        When("GET /assistant/artifacts를 호출하면") {
            Then("200 OK와 아티팩트 목록이 반환된다") {
                client.get()
                    .uri("/assistant/artifacts?workspace=$workspace")
                    .header("Accept", "application/vnd.sayaya.handbook.v1+json")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .jsonPath("$.length()").isEqualTo(1)
                    .jsonPath("$[0].artifact.summary").isEqualTo("고객 타입에 이메일 필드 추가")
                    .jsonPath("$[0].artifact.changes.length()").isEqualTo(2)
                    .jsonPath("$[0].status").isEqualTo("COMPLETED")
            }
        }
    }

    Given("아티팩트가 없는 워크스페이스에서 아티팩트를 조회할 때 (UC-A10)") {
        val workspace = UUID.randomUUID()
        every { service.getArtifacts(workspace) } returns Flux.empty()

        When("GET /assistant/artifacts를 호출하면") {
            Then("200 OK와 빈 목록이 반환된다") {
                client.get()
                    .uri("/assistant/artifacts?workspace=$workspace")
                    .header("Accept", "application/vnd.sayaya.handbook.v1+json")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .jsonPath("$.length()").isEqualTo(0)
            }
        }
    }
})
