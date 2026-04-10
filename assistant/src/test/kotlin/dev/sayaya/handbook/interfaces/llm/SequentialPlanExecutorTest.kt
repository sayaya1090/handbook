package dev.sayaya.handbook.interfaces.llm

import dev.sayaya.handbook.domain.AgentCommand
import dev.sayaya.handbook.domain.CommandType
import dev.sayaya.handbook.domain.ExecutionPlan
import dev.sayaya.handbook.domain.ExecutionStep
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import reactor.test.StepVerifier

class SequentialPlanExecutorTest : BehaviorSpec({
    val executor = SequentialPlanExecutor()

    Given("단일 단계 실행 계획이 주어졌을 때") {
        val command = AgentCommand(type = CommandType.NAVIGATE, target = "type-management")
        val plan = ExecutionPlan(
            intent = "타입 관리 이동",
            steps = listOf(
                ExecutionStep(order = 0, command = command, description = "타입 관리 화면으로 이동"),
            ),
            confidence = 0.9,
        )

        When("execute를 호출하면") {
            val result = executor.execute(plan)

            Then("PROGRESS, 커맨드, COMPLETE 순서로 발행된다") {
                StepVerifier.create(result)
                    .assertNext {
                        it.type shouldBe CommandType.PROGRESS
                        it.payload!!["current"] shouldBe 1
                        it.payload!!["total"] shouldBe 1
                    }
                    .assertNext {
                        it.type shouldBe CommandType.NAVIGATE
                        it.target shouldBe "type-management"
                    }
                    .assertNext {
                        it.type shouldBe CommandType.COMPLETE
                        it.payload!!["intent"] shouldBe "타입 관리 이동"
                    }
                    .verifyComplete()
            }
        }
    }

    Given("복수 단계 실행 계획이 주어졌을 때") {
        val navigateCmd = AgentCommand(type = CommandType.NAVIGATE, target = "type-management")
        val mutateCmd = AgentCommand(
            type = CommandType.MUTATE,
            payload = mapOf("field" to "name", "value" to "고객"),
        )
        val notifyCmd = AgentCommand(
            type = CommandType.NOTIFY,
            payload = mapOf("level" to "info", "message" to "수정 완료"),
        )
        val plan = ExecutionPlan(
            intent = "타입 정의 수정",
            steps = listOf(
                ExecutionStep(order = 0, command = navigateCmd, description = "이동"),
                ExecutionStep(order = 1, command = mutateCmd, description = "수정"),
                ExecutionStep(order = 2, command = notifyCmd, description = "알림"),
            ),
            confidence = 0.85,
        )

        When("execute를 호출하면") {
            val result = executor.execute(plan)

            Then("각 단계마다 PROGRESS + 커맨드가 발행되고 마지막에 COMPLETE가 발행된다") {
                StepVerifier.create(result)
                    // Step 0: PROGRESS + NAVIGATE
                    .assertNext { it.type shouldBe CommandType.PROGRESS }
                    .assertNext { it.type shouldBe CommandType.NAVIGATE }
                    // Step 1: PROGRESS + MUTATE
                    .assertNext { it.type shouldBe CommandType.PROGRESS }
                    .assertNext { it.type shouldBe CommandType.MUTATE }
                    // Step 2: PROGRESS + NOTIFY
                    .assertNext { it.type shouldBe CommandType.PROGRESS }
                    .assertNext { it.type shouldBe CommandType.NOTIFY }
                    // COMPLETE
                    .assertNext { it.type shouldBe CommandType.COMPLETE }
                    .verifyComplete()
            }
        }
    }

    Given("빈 실행 계획이 주어졌을 때") {
        val plan = ExecutionPlan(
            intent = "알 수 없음",
            steps = emptyList(),
            confidence = 0.1,
        )

        When("execute를 호출하면") {
            val result = executor.execute(plan)

            Then("COMPLETE 커맨드만 발행된다") {
                StepVerifier.create(result)
                    .assertNext { it.type shouldBe CommandType.COMPLETE }
                    .verifyComplete()
            }
        }
    }

    Given("순서가 뒤섞인 단계가 주어졌을 때") {
        val cmdA = AgentCommand(type = CommandType.NAVIGATE, target = "a")
        val cmdB = AgentCommand(type = CommandType.HIGHLIGHT, target = "b")
        val plan = ExecutionPlan(
            intent = "순서 테스트",
            steps = listOf(
                ExecutionStep(order = 1, command = cmdB, description = "두 번째"),
                ExecutionStep(order = 0, command = cmdA, description = "첫 번째"),
            ),
            confidence = 0.8,
        )

        When("execute를 호출하면") {
            val result = executor.execute(plan)

            Then("order 기준으로 정렬되어 실행된다") {
                StepVerifier.create(result)
                    .assertNext { it.type shouldBe CommandType.PROGRESS }
                    .assertNext {
                        it.type shouldBe CommandType.NAVIGATE
                        it.target shouldBe "a"
                    }
                    .assertNext { it.type shouldBe CommandType.PROGRESS }
                    .assertNext {
                        it.type shouldBe CommandType.HIGHLIGHT
                        it.target shouldBe "b"
                    }
                    .assertNext { it.type shouldBe CommandType.COMPLETE }
                    .verifyComplete()
            }
        }
    }
})
