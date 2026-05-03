package dev.sayaya.handbook.interfaces.llm

import dev.sayaya.handbook.domain.AgentCommand
import dev.sayaya.handbook.domain.CommandType
import dev.sayaya.handbook.domain.ExecutionPlan
import dev.sayaya.handbook.domain.ExecutionStep
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import reactor.test.StepVerifier

class GroupedPlanExecutorTest : BehaviorSpec({
    val executor = GroupedPlanExecutor()

    Given("단일 그룹, 단일 단계 실행 계획이 주어졌을 때") {
        val command = AgentCommand(type = CommandType.NAVIGATE, target = "type-management")
        val plan = ExecutionPlan(
            intent = "타입 관리 이동",
            steps = listOf(
                ExecutionStep(group = 0, order = 0, command = command, description = "타입 관리 화면으로 이동"),
            ),
            confidence = 0.9,
        )

        When("execute를 호출하면") {
            val result = executor.execute(plan)

            Then("PROGRESS, 커맨드, COMPLETE 순서로 발행된다") {
                StepVerifier.create(result)
                    .assertNext {
                        it.type shouldBe CommandType.PROGRESS
                        it.payload!!["currentGroup"] shouldBe 1
                        it.payload["totalGroups"] shouldBe 1
                        it.payload["parallel"] shouldBe false
                        it.payload["stepCount"] shouldBe 1
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

    Given("서로 다른 그룹의 순차 단계가 주어졌을 때") {
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
                ExecutionStep(group = 0, order = 0, command = navigateCmd, description = "이동"),
                ExecutionStep(group = 1, order = 1, command = mutateCmd, description = "수정"),
                ExecutionStep(group = 2, order = 2, command = notifyCmd, description = "알림"),
            ),
            confidence = 0.85,
        )

        When("execute를 호출하면") {
            val result = executor.execute(plan)

            Then("각 그룹마다 PROGRESS + 커맨드가 발행되고 마지막에 COMPLETE가 발행된다") {
                StepVerifier.create(result)
                    .assertNext { it.type shouldBe CommandType.PROGRESS }
                    .assertNext { it.type shouldBe CommandType.NAVIGATE }
                    .assertNext { it.type shouldBe CommandType.PROGRESS }
                    .assertNext { it.type shouldBe CommandType.MUTATE }
                    .assertNext { it.type shouldBe CommandType.PROGRESS }
                    .assertNext { it.type shouldBe CommandType.NOTIFY }
                    .assertNext { it.type shouldBe CommandType.COMPLETE }
                    .verifyComplete()
            }
        }
    }

    Given("같은 그룹에 여러 단계가 있을 때 (병렬)") {
        val highlightCmd = AgentCommand(type = CommandType.HIGHLIGHT, target = ".field-a")
        val attentionCmd = AgentCommand(type = CommandType.ATTENTION, target = ".field-b")
        val plan = ExecutionPlan(
            intent = "필드 강조",
            steps = listOf(
                ExecutionStep(group = 0, order = 0, command = highlightCmd, description = "필드 A 강조"),
                ExecutionStep(group = 0, order = 1, command = attentionCmd, description = "필드 B 주목"),
            ),
            confidence = 0.9,
        )

        When("execute를 호출하면") {
            val result = executor.execute(plan)

            Then("PROGRESS에 parallel=true로 표시되고 두 커맨드가 발행된다") {
                StepVerifier.create(result)
                    .assertNext {
                        it.type shouldBe CommandType.PROGRESS
                        it.payload!!["parallel"] shouldBe true
                        it.payload["stepCount"] shouldBe 2
                    }
                    .thenConsumeWhile { it.type != CommandType.COMPLETE }
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

    Given("그룹 번호가 뒤섞인 단계가 주어졌을 때") {
        val cmdA = AgentCommand(type = CommandType.NAVIGATE, target = "a")
        val cmdB = AgentCommand(type = CommandType.HIGHLIGHT, target = "b")
        val plan = ExecutionPlan(
            intent = "순서 테스트",
            steps = listOf(
                ExecutionStep(group = 1, order = 1, command = cmdB, description = "두 번째 그룹"),
                ExecutionStep(group = 0, order = 0, command = cmdA, description = "첫 번째 그룹"),
            ),
            confidence = 0.8,
        )

        When("execute를 호출하면") {
            val result = executor.execute(plan)

            Then("group 기준으로 정렬되어 실행된다") {
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
