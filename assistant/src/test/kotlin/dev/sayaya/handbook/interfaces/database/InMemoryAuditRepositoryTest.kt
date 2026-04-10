package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.*
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import reactor.test.StepVerifier
import java.util.UUID

class InMemoryAuditRepositoryTest : BehaviorSpec({
    val repo = InMemoryAuditRepository()

    Given("감사 기록을 저장할 때") {
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
            status = AuditEntry.Status.REQUESTED,
        )

        When("save를 호출하면") {
            val result = repo.save(entry)

            Then("저장된 감사 기록이 반환된다") {
                StepVerifier.create(result)
                    .assertNext {
                        it.id shouldBe entry.id
                        it.workspace shouldBe workspace
                        it.userMessage shouldBe "고객 타입 수정해줘"
                        it.status shouldBe AuditEntry.Status.REQUESTED
                    }
                    .verifyComplete()
            }
        }

        When("워크스페이스로 조회하면") {
            Then("저장된 기록이 조회된다") {
                StepVerifier.create(repo.findByWorkspace(workspace))
                    .assertNext {
                        it.workspace shouldBe workspace
                        it.userMessage shouldBe "고객 타입 수정해줘"
                    }
                    .verifyComplete()
            }
        }

        When("상태를 갱신하면") {
            val updateResult = repo.updateStatus(entry.id, AuditEntry.Status.EXECUTING)

            Then("상태가 변경된다") {
                StepVerifier.create(updateResult).verifyComplete()

                StepVerifier.create(repo.findByWorkspace(workspace))
                    .assertNext {
                        it.id shouldBe entry.id
                        it.status shouldBe AuditEntry.Status.EXECUTING
                    }
                    .verifyComplete()
            }
        }
    }

    Given("다른 워크스페이스의 기록이 있을 때") {
        val workspace1 = UUID.randomUUID()
        val workspace2 = UUID.randomUUID()
        val plan = ExecutionPlan(intent = "테스트", steps = emptyList(), confidence = 0.5)
        val entry1 = AuditEntry(
            workspace = workspace1,
            userMessage = "메시지1",
            intent = "의도1",
            confidence = 0.8,
            plan = plan,
            status = AuditEntry.Status.COMPLETED,
        )
        val entry2 = AuditEntry(
            workspace = workspace2,
            userMessage = "메시지2",
            intent = "의도2",
            confidence = 0.7,
            plan = plan,
            status = AuditEntry.Status.REQUESTED,
        )

        When("각각 저장 후 워크스페이스1로 조회하면") {
            repo.save(entry1).block()
            repo.save(entry2).block()

            Then("해당 워크스페이스의 기록만 반환된다") {
                StepVerifier.create(repo.findByWorkspace(workspace1))
                    .assertNext { it.workspace shouldBe workspace1 }
                    .verifyComplete()
            }
        }
    }
})
