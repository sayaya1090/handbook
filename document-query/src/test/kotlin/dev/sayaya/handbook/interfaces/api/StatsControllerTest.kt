package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.usecase.AgentActivityEntry
import dev.sayaya.handbook.usecase.DistributionEntry
import dev.sayaya.handbook.usecase.QualityIssueEntry
import dev.sayaya.handbook.usecase.StatsService
import dev.sayaya.handbook.usecase.TimelineEntry
import dev.sayaya.handbook.usecase.WorkspaceSummary
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

class StatsControllerTest : BehaviorSpec({
    val service = mockk<StatsService>()
    val controller = StatsController(service)
    val client = WebTestClient.bindToController(controller).build()
    val workspace = UUID.randomUUID()

    Given("타임라인 통계 API") {
        val entries = listOf(
            TimelineEntry("2026-01-01", 10, 2, 1),
            TimelineEntry("2026-02-01", 15, 0, 3),
        )
        every { service.timeline(workspace, any<Instant>(), any<Instant>(), any()) } returns Flux.fromIterable(entries)

        When("GET /workspaces/{workspace}/stats/timeline을 호출하면") {
            Then("200 OK가 반환된다") {
                client.get()
                    .uri("/workspaces/$workspace/stats/timeline?from=2026-01-01T00:00:00Z&to=2026-03-31T23:59:59Z&interval=1")
                    .header("Accept", "application/json")
                    .exchange()
                    .expectStatus().isOk
            }
        }
    }

    Given("타입별 분포 API") {
        val entries = listOf(
            DistributionEntry("customer", 25),
            DistributionEntry("invoice", 42),
        )
        every { service.distribution(workspace) } returns Flux.fromIterable(entries)

        When("GET /workspaces/{workspace}/stats/distribution을 호출하면") {
            Then("200 OK가 반환된다") {
                client.get()
                    .uri("/workspaces/$workspace/stats/distribution")
                    .header("Accept", "application/json")
                    .exchange()
                    .expectStatus().isOk
            }
        }
    }

    Given("워크스페이스 집계 통계 API") {
        val summary = WorkspaceSummary(5, 120, 8)
        every { service.summary(workspace) } returns Mono.just(summary)

        When("GET /workspaces/{workspace}/stats를 호출하면") {
            Then("200 OK와 집계 통계가 반환된다") {
                client.get()
                    .uri("/workspaces/$workspace/stats")
                    .header("Accept", "application/json")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .jsonPath("$.typeCount").isEqualTo(5)
                    .jsonPath("$.documentCount").isEqualTo(120)
                    .jsonPath("$.userCount").isEqualTo(8)
            }
        }
    }

    Given("품질 이슈 API") {
        val issues = listOf(
            QualityIssueEntry("doc-1", "SER-001", "invoice", "필수 필드 누락", "error"),
            QualityIssueEntry("doc-2", "SER-002", "customer", "형식 불일치", "warning"),
        )
        every { service.qualityIssues(workspace) } returns Flux.fromIterable(issues)

        When("GET /workspaces/{workspace}/quality-issues를 호출하면") {
            Then("200 OK와 이슈 목록이 반환된다") {
                client.get()
                    .uri("/workspaces/$workspace/quality-issues")
                    .header("Accept", "application/json")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .jsonPath("$[0].severity").isEqualTo("error")
            }
        }
    }

    Given("에이전트 활동 API") {
        val activities = listOf(
            AgentActivityEntry("agent-1", "document_change", "invoice", "2026-04-01T10:00:00Z", 15),
        )
        every { service.agentActivity(workspace) } returns Flux.fromIterable(activities)

        When("GET /workspaces/{workspace}/agent-activity를 호출하면") {
            Then("200 OK와 활동 목록이 반환된다") {
                client.get()
                    .uri("/workspaces/$workspace/agent-activity")
                    .header("Accept", "application/json")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .jsonPath("$[0].agentId").isEqualTo("agent-1")
                    .jsonPath("$[0].count").isEqualTo(15)
            }
        }
    }
})
