package dev.sayaya.handbook.interfaces.schedule

import dev.sayaya.handbook.usecase.QualityMonitorService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import java.util.UUID

/**
 * 스케줄 기반 품질 감시를 수행한다.
 *
 * **책임:** cron 주기에 따라 등록된 모든 활성 워크스페이스에 대해
 * [QualityMonitorService.execute]를 호출하여 품질 이슈를 자동 스캔한다.
 *
 * **의존관계:**
 * - [QualityMonitorService] -- 실제 품질 검증 로직 수행 및 이슈 발행
 * - [WorkspaceProvider] -- 활성 워크스페이스 목록 제공
 *
 * **주의:** cron 표현식은 `quality.monitor.cron` 프로퍼티로 설정 가능하며,
 * 기본값은 매 시간 정각(`0 0 * * * *`)이다.
 */
class ScheduledQualityMonitor(
    private val qualityMonitorService: QualityMonitorService,
    private val workspaceProvider: WorkspaceProvider,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "\${quality.monitor.cron:0 0 * * * *}")
    fun scanAll() {
        logger.info("Scheduled quality scan started")
        val workspaces = workspaceProvider.getActiveWorkspaces()
        workspaces.forEach { workspace ->
            logger.info("Scanning workspace: {}", workspace)
            qualityMonitorService.execute(workspace)
                .doOnError { e -> logger.error("Quality scan failed for workspace {}: {}", workspace, e.message) }
                .subscribe()
        }
        logger.info("Scheduled quality scan completed for {} workspace(s)", workspaces.size)
    }
}
