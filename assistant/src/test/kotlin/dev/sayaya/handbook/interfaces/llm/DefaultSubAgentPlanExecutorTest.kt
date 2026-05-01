package dev.sayaya.handbook.interfaces.llm

import dev.sayaya.handbook.domain.*
import dev.sayaya.handbook.usecase.AgentCommandEventPublisher
import dev.sayaya.handbook.usecase.IntentParser
import dev.sayaya.handbook.usecase.PlanExecutor
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.*

class DefaultSubAgentPlanExecutorTest : BehaviorSpec({
    val intentParser = mockk<IntentParser>()
    val planExecutor = mockk<PlanExecutor>()
    val eventPublisher = mockk<AgentCommandEventPublisher>(relaxed = true)
    val executor = DefaultSubAgentPlanExecutor(intentParser, planExecutor, eventPublisher)

    Given("서브 에이전트 정의가 주어졌을 때") {
        val workspace = UUID.randomUUID()
        val parentExecutionId = UUID.randomUUID()
        val definition = SubAgentDefinition(
            name = "schema-analyst",
            role = "스키마 분석 전문가",
            task = "고객 타입의 스키마를 분석하라",
        )
        val subPlan = ExecutionPlan(
            intent = "스키마 분석",
            steps = listOf(
                ExecutionStep(group = 0, order = 0, command = AgentCommand(type = CommandType.NAVIGATE, target = "type-viewer"), description = "타입 뷰어로 이동"),
            ),
            confidence = 0.9,
            subAgents = listOf(SubAgentDefinition(name = "nested", role = "x", task = "y")),
        )

        every { intentParser.parse(any(), any()) } returns Mono.just(subPlan)
        every { planExecutor.execute(any()) } returns Flux.just(
            AgentCommand(type = CommandType.PROGRESS, payload = mapOf("currentGroup" to 1, "totalGroups" to 1)),
            AgentCommand(type = CommandType.NAVIGATE, target = "type-viewer"),
            AgentCommand(type = CommandType.COMPLETE, payload = mapOf("intent" to "스키마 분석")),
        )

        When("execute를 호출하면") {
            val result = executor.execute(workspace, parentExecutionId, definition, emptyMap())

            Then("서브 에이전트의 subAgents는 제거되어 실행된다") {
                StepVerifier.create(result)
                    .assertNext { artifact ->
                        artifact.executionId shouldBe parentExecutionId
                        artifact.summary shouldContain "schema-analyst"
                        artifact.summary shouldContain "스키마 분석"
                        artifact.changes.size shouldBe 1
                        artifact.changes[0].type shouldBe "NAVIGATE"
                    }
                    .verifyComplete()

                verify { planExecutor.execute(match { it.subAgents.isEmpty() }) }
            }
        }

        When("PROGRESS 이벤트에 서브 에이전트 이름이 포함된다") {
            StepVerifier.create(executor.execute(workspace, parentExecutionId, definition, emptyMap()))
                .expectNextCount(1)
                .verifyComplete()

            Then("eventPublisher에 subAgentName이 포함된 PROGRESS가 발행된다") {
                verify {
                    eventPublisher.publish(workspace, any(), match {
                        it.type == CommandType.PROGRESS && it.payload?.get("subAgentName") == "schema-analyst"
                    })
                }
            }
        }
    }

    Given("IntentParser.parse()가 실패할 때") {
        val workspace = UUID.randomUUID()
        val parentExecutionId = UUID.randomUUID()
        val definition = SubAgentDefinition(
            name = "failing-parser",
            role = "실패하는 파서",
            task = "파싱 실패 태스크",
        )

        every { intentParser.parse(any(), any()) } returns Mono.error(RuntimeException("LLM 파싱 실패"))

        When("execute를 호출하면") {
            val result = executor.execute(workspace, parentExecutionId, definition, emptyMap())

            Then("에러가 전파된다") {
                StepVerifier.create(result)
                    .expectErrorMatches { it is RuntimeException && it.message == "LLM 파싱 실패" }
                    .verify()
            }
        }
    }

    Given("PlanExecutor.execute()가 실패할 때") {
        val workspace = UUID.randomUUID()
        val parentExecutionId = UUID.randomUUID()
        val definition = SubAgentDefinition(
            name = "failing-executor",
            role = "실패하는 실행기",
            task = "실행 실패 태스크",
        )
        val subPlan = ExecutionPlan(
            intent = "실패할 계획",
            steps = listOf(
                ExecutionStep(group = 0, order = 0, command = AgentCommand(type = CommandType.NAVIGATE, target = "target"), description = "이동"),
            ),
            confidence = 0.8,
        )

        every { intentParser.parse(any(), any()) } returns Mono.just(subPlan)
        every { planExecutor.execute(any()) } returns Flux.error(RuntimeException("실행 중 에러 발생"))

        When("execute를 호출하면") {
            val result = executor.execute(workspace, parentExecutionId, definition, emptyMap())

            Then("에러가 전파된다") {
                StepVerifier.create(result)
                    .expectErrorMatches { it is RuntimeException && it.message == "실행 중 에러 발생" }
                    .verify()
            }
        }
    }

    Given("상위 아티팩트가 있을 때") {
        val workspace = UUID.randomUUID()
        val parentExecutionId = UUID.randomUUID()
        val definition = SubAgentDefinition(
            name = "report-writer",
            role = "보고서 작성자",
            task = "분석 결과를 요약하라",
            dependsOn = listOf("schema-analyst"),
        )
        val upstreamArtifact = Artifact(
            executionId = parentExecutionId,
            summary = "스키마 분석 완료",
            changes = listOf(ArtifactChange(type = "NAVIGATE", target = "type-viewer", description = "이동")),
        )
        val subPlan = ExecutionPlan(
            intent = "보고서 작성",
            steps = listOf(
                ExecutionStep(group = 0, order = 0, command = AgentCommand(type = CommandType.NOTIFY, payload = mapOf("message" to "완료")), description = "알림"),
            ),
            confidence = 0.85,
        )

        every { intentParser.parse(any(), any()) } returns Mono.just(subPlan)
        every { planExecutor.execute(any()) } returns Flux.just(
            AgentCommand(type = CommandType.PROGRESS, payload = mapOf("currentGroup" to 1, "totalGroups" to 1)),
            AgentCommand(type = CommandType.NOTIFY, payload = mapOf("message" to "완료")),
            AgentCommand(type = CommandType.COMPLETE, payload = mapOf("intent" to "보고서 작성")),
        )

        When("상위 아티팩트를 전달하여 실행하면") {
            val result = executor.execute(workspace, parentExecutionId, definition, mapOf("schema-analyst" to upstreamArtifact))

            Then("context에 상위 아티팩트 정보가 포함된다") {
                StepVerifier.create(result)
                    .assertNext { artifact ->
                        artifact.summary shouldContain "report-writer"
                    }
                    .verifyComplete()

                verify {
                    intentParser.parse(any(), match { ctx ->
                        ctx != null && ctx.contains("schema-analyst") && ctx.contains("스키마 분석 완료")
                    })
                }
            }
        }
    }
})
