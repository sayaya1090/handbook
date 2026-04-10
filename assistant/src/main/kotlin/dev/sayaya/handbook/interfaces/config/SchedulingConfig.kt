package dev.sayaya.handbook.interfaces.config

import dev.sayaya.handbook.interfaces.schedule.ScheduledQualityMonitor
import dev.sayaya.handbook.interfaces.schedule.WebClientWorkspaceProvider
import dev.sayaya.handbook.interfaces.schedule.WorkspaceProvider
import dev.sayaya.handbook.usecase.QualityMonitorService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.reactive.function.client.WebClient

/**
 * 스케줄링 관련 설정.
 *
 * **책임:** Spring의 @EnableScheduling을 활성화하고,
 * [ScheduledQualityMonitor] 빈을 등록한다.
 *
 * **의존관계:**
 * - [QualityMonitorService] -- 품질 감시 서비스
 * - [WorkspaceProvider] -- 활성 워크스페이스 목록 제공
 */
@Configuration
@EnableScheduling
class SchedulingConfig {
    @Bean
    fun workspaceProvider(
        @Value("\${handbook.workspace.base-url:http://localhost:8080}") baseUrl: String,
    ): WorkspaceProvider = WebClientWorkspaceProvider(
        WebClient.builder().baseUrl(baseUrl).build()
    )

    @Bean
    fun scheduledQualityMonitor(
        qualityMonitorService: QualityMonitorService,
        workspaceProvider: WorkspaceProvider,
    ) = ScheduledQualityMonitor(qualityMonitorService, workspaceProvider)
}
