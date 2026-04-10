package dev.sayaya.handbook.interfaces.config

import dev.sayaya.handbook.usecase.GroupRepository
import dev.sayaya.handbook.usecase.WorkspaceEventPublisher
import dev.sayaya.handbook.usecase.WorkspaceRepository
import dev.sayaya.handbook.usecase.WorkspaceService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing

@Configuration
@EnableR2dbcAuditing
class WorkspaceConfig {
    @Bean
    fun workspaceService(
        workspaceRepo: WorkspaceRepository,
        groupRepo: GroupRepository,
        eventPublisher: WorkspaceEventPublisher,
    ) = WorkspaceService(workspaceRepo, groupRepo, eventPublisher)
}
