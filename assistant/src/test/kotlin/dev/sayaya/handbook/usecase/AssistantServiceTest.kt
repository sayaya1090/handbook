package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.*
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.UUID

class AssistantServiceTest : BehaviorSpec({
    val intentParser = mockk<IntentParser>()
    val planExecutor = mockk<PlanExecutor>()
    val eventPublisher = mockk<AgentCommandEventPublisher>(relaxed = true)
    val auditRepository = mockk<AuditRepository>(relaxed = true)
    val service = AssistantService(intentParser, planExecutor, eventPublisher, auditRepository)

    Given("자연어 메시지가 주어졌을 때") {
        val workspace = UUID.randomUUID()
        val message = "고객 타입 정의를 수정해줘"
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
        every { intentParser.parse(message) } returns Mono.just(plan)
        every { auditRepository.save(any()) } answers {
            Mono.just(firstArg<AuditEntry>())
        }

        When("request를 호출하면") {
            val result = service.request(workspace, message)

            Then("실행 계획이 반환된다") {
                StepVerifier.create(result)
                    .assertNext {
                        it.intent shouldBe "타입 정의 수정"
                        it.confidence shouldBe 0.95
                        it.steps.size shouldBe 1
                    }
                    .verifyComplete()
            }
            Then("IntentParser.parse가 호출된다") {
                verify { intentParser.parse(message) }
            }
            Then("감사 기록이 REQUESTED 상태로 저장된다") {
                verify { auditRepository.save(match {
                    it.workspace == workspace &&
                    it.userMessage == message &&
                    it.intent == "타입 정의 수정" &&
                    it.status == AuditEntry.Status.REQUESTED
                }) }
            }
        }
    }

    Given("실행 계획이 주어졌을 때") {
        val workspace = UUID.randomUUID()
        val navigateCmd = AgentCommand(type = CommandType.NAVIGATE, target = "type-management")
        val notifyCmd = AgentCommand(
            type = CommandType.NOTIFY,
            payload = mapOf("level" to "info", "message" to "완료"),
        )
        val plan = ExecutionPlan(
            intent = "타입 정의 수정",
            steps = listOf(
                ExecutionStep(order = 0, command = navigateCmd, description = "이동"),
                ExecutionStep(order = 1, command = notifyCmd, description = "알림"),
            ),
            confidence = 0.9,
        )
        every { planExecutor.execute(plan) } returns Flux.just(navigateCmd, notifyCmd)
        every { auditRepository.updateStatus(any(), any()) } returns Mono.empty()

        When("execute를 호출하면") {
            val result = service.execute(workspace, plan)

            Then("Kafka 이벤트가 발행된다") {
                StepVerifier.create(result).verifyComplete()
                Thread.sleep(100) // 비동기 subscribe 완료 대기
                verify(atLeast = 1) { eventPublisher.publish(workspace, any(), any()) }
            }
        }
    }

    Given("실행 중인 계획이 있을 때") {
        When("abort를 호출하면") {
            val result = service.abort()

            Then("성공적으로 완료된다") {
                StepVerifier.create(result)
                    .verifyComplete()
            }
        }
    }
})
