package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.CommandType
import dev.sayaya.handbook.domain.QualityIssue
import dev.sayaya.handbook.domain.QualityIssue.Severity
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.util.*

class QualityMonitorServiceTest : BehaviorSpec({
    val monitor = mockk<QualityMonitor>()
    val eventPublisher = mockk<AgentCommandEventPublisher>(relaxed = true)
    val service = QualityMonitorService(monitor, eventPublisher)

    Given("품질 이슈가 발견된 워크스페이스가 주어졌을 때") {
        val workspace = UUID.randomUUID()
        val issues = listOf(
            QualityIssue(
                type = "고객",
                serial = "C-001",
                field = "이메일",
                severity = Severity.ERROR,
                message = "필수 필드 '이메일'에 값이 없습니다",
            ),
            QualityIssue(
                type = "주문",
                serial = "O-100",
                field = "금액",
                severity = Severity.WARNING,
                message = "필드 '금액' 값이 평균에서 3σ 이상 벗어났습니다",
            ),
            QualityIssue(
                type = "제품",
                serial = "P-050",
                field = null,
                severity = Severity.INFO,
                message = "타입 '제품'에서 시리얼 'P-050'이 2건 중복됩니다",
            ),
        )
        every { monitor.scan(workspace) } returns Flux.fromIterable(issues)

        When("execute를 호출하면") {
            val result = service.execute(workspace)

            Then("각 이슈에 대해 NOTIFY 이벤트가 발행된다") {
                StepVerifier.create(result).verifyComplete()

                verify(exactly = 3) { eventPublisher.publish(workspace, 0, any()) }
            }

            Then("심각도에 따라 올바른 level이 설정된다") {
                verify { eventPublisher.publish(workspace, 0, match {
                    it.type == CommandType.NOTIFY &&
                    it.payload?.get("level") == "error" &&
                    it.payload?.get("type") == "고객"
                }) }
                verify { eventPublisher.publish(workspace, 0, match {
                    it.type == CommandType.NOTIFY &&
                    it.payload?.get("level") == "warning" &&
                    it.payload?.get("type") == "주문"
                }) }
                verify { eventPublisher.publish(workspace, 0, match {
                    it.type == CommandType.NOTIFY &&
                    it.payload?.get("level") == "info" &&
                    it.payload?.get("type") == "제품"
                }) }
            }
        }
    }

    Given("품질 이슈가 없는 워크스페이스가 주어졌을 때") {
        val workspace = UUID.randomUUID()
        every { monitor.scan(workspace) } returns Flux.empty()

        When("execute를 호출하면") {
            val result = service.execute(workspace)

            Then("이벤트가 발행되지 않고 정상 완료된다") {
                StepVerifier.create(result).verifyComplete()
                verify(exactly = 0) { eventPublisher.publish(workspace, any(), any()) }
            }
        }
    }
})
