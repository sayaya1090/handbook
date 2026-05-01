package dev.sayaya.handbook.interfaces.schedule

import dev.sayaya.handbook.usecase.AgentCommandEventPublisher
import dev.sayaya.handbook.usecase.QualityMonitor
import dev.sayaya.handbook.usecase.QualityMonitorService
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import reactor.core.publisher.Flux
import java.util.*

class ScheduledQualityMonitorTest : BehaviorSpec({
    val monitor = mockk<QualityMonitor>()
    val eventPublisher = mockk<AgentCommandEventPublisher>(relaxed = true)
    val qualityMonitorService = QualityMonitorService(monitor, eventPublisher)
    val workspaceProvider = mockk<WorkspaceProvider>()
    val scheduler = ScheduledQualityMonitor(qualityMonitorService, workspaceProvider)

    Given("활성 워크스페이스가 2개 존재할 때") {
        val ws1 = UUID.randomUUID()
        val ws2 = UUID.randomUUID()
        every { workspaceProvider.getActiveWorkspaces() } returns listOf(ws1, ws2)
        every { monitor.scan(ws1) } returns Flux.empty()
        every { monitor.scan(ws2) } returns Flux.empty()

        When("scanAll을 호출하면") {
            scheduler.scanAll()

            Then("각 워크스페이스에 대해 scan이 호출된다") {
                verify(exactly = 1) { monitor.scan(ws1) }
                verify(exactly = 1) { monitor.scan(ws2) }
            }
        }
    }

    Given("활성 워크스페이스가 없을 때") {
        clearMocks(monitor, answers = false)
        every { workspaceProvider.getActiveWorkspaces() } returns emptyList()

        When("scanAll을 호출하면") {
            scheduler.scanAll()

            Then("scan이 호출되지 않는다") {
                verify(exactly = 0) { monitor.scan(any()) }
            }
        }
    }
})
