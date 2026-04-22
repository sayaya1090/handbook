package dev.sayaya.handbook.domain

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.util.*

class DomainTest : DescribeSpec({
    describe("Domain Objects") {
        it("AgentCommand를 생성할 수 있다") {
            val cmd = AgentCommand(CommandType.NAVIGATE, "#target", mapOf("url" to "/"))
            cmd.type shouldBe CommandType.NAVIGATE
            cmd.target shouldBe "#target"
            cmd.payload shouldBe mapOf("url" to "/")
        }
        it("ExecutionPlan을 생성할 수 있다") {
            val step = ExecutionStep(0, 0, AgentCommand(CommandType.NOTIFY), "desc")
            val plan = ExecutionPlan("intent", listOf(step), 0.9)
            plan.intent shouldBe "intent"
            plan.steps.size shouldBe 1
            plan.confidence shouldBe 0.9
        }
        it("Artifact를 생성할 수 있다") {
            val id = UUID.randomUUID()
            val change = ArtifactChange("MUTATE", "box", "added")
            val artifact = Artifact(id, "summary", listOf(change))
            artifact.executionId shouldBe id
            artifact.summary shouldBe "summary"
            artifact.changes shouldBe listOf(change)
        }
        it("SubAgentDefinition을 생성할 수 있다") {
            val sub = SubAgentDefinition("name", "role", "task")
            sub.name shouldBe "name"
            sub.role shouldBe "role"
            sub.task shouldBe "task"
        }
        it("QualityIssue를 생성할 수 있다") {
            val issue = QualityIssue("type", "serial", "field", QualityIssue.Severity.ERROR, "msg")
            issue.severity shouldBe QualityIssue.Severity.ERROR
            issue.message shouldBe "msg"
        }
    }
})
