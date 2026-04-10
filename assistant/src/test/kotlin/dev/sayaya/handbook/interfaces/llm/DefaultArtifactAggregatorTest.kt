package dev.sayaya.handbook.interfaces.llm

import dev.sayaya.handbook.domain.Artifact
import dev.sayaya.handbook.domain.ArtifactChange
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.util.UUID

class DefaultArtifactAggregatorTest : BehaviorSpec({
    val aggregator = DefaultArtifactAggregator()

    Given("여러 서브 에이전트의 Artifact가 주어졌을 때") {
        val executionId = UUID.randomUUID()
        val artifact1 = Artifact(
            executionId = executionId,
            summary = "스키마 분석 완료",
            changes = listOf(
                ArtifactChange(type = "NAVIGATE", target = "type-viewer", description = "이동"),
                ArtifactChange(type = "HIGHLIGHT", target = ".field-a", description = "강조"),
            ),
        )
        val artifact2 = Artifact(
            executionId = executionId,
            summary = "보고서 작성 완료",
            changes = listOf(
                ArtifactChange(type = "NOTIFY", target = "", description = "알림"),
            ),
        )
        val subArtifacts = mapOf("schema-analyst" to artifact1, "report-writer" to artifact2)

        When("aggregate를 호출하면") {
            val result = aggregator.aggregate(executionId, "복합 태스크 실행", subArtifacts)

            Then("모든 changes가 병합된다") {
                result.changes.size shouldBe 3
                result.changes[0].type shouldBe "NAVIGATE"
                result.changes[1].type shouldBe "HIGHLIGHT"
                result.changes[2].type shouldBe "NOTIFY"
            }

            Then("summary에 서브 에이전트 이름 헤더가 포함된다") {
                result.summary shouldContain "복합 태스크 실행"
                result.summary shouldContain "[schema-analyst]"
                result.summary shouldContain "[report-writer]"
            }

            Then("executionId가 부모 ID와 동일하다") {
                result.executionId shouldBe executionId
            }
        }
    }

    Given("빈 서브 에이전트 목록이 주어졌을 때") {
        val executionId = UUID.randomUUID()

        When("aggregate를 호출하면") {
            val result = aggregator.aggregate(executionId, "빈 태스크", emptyMap())

            Then("intent만 summary에 포함된다") {
                result.summary shouldBe "빈 태스크"
                result.changes shouldBe emptyList()
            }
        }
    }

    Given("단일 서브 에이전트 Artifact가 주어졌을 때") {
        val executionId = UUID.randomUUID()
        val artifact = Artifact(
            executionId = executionId,
            summary = "단일 작업 완료",
            changes = listOf(ArtifactChange(type = "MUTATE", target = "field-x", description = "수정")),
        )

        When("aggregate를 호출하면") {
            val result = aggregator.aggregate(executionId, "단일 태스크", mapOf("worker" to artifact))

            Then("changes가 그대로 포함된다") {
                result.changes.size shouldBe 1
                result.changes[0].type shouldBe "MUTATE"
            }

            Then("summary에 서브 에이전트 헤더가 포함된다") {
                result.summary shouldContain "[worker]"
                result.summary shouldContain "단일 태스크"
            }
        }
    }
})
