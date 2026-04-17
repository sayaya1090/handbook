package dev.sayaya.handbook.interfaces.config

import dev.sayaya.handbook.usecase.GroupRepository
import dev.sayaya.handbook.usecase.WebhookService
import dev.sayaya.handbook.usecase.WorkspaceEventPublisher
import dev.sayaya.handbook.usecase.WorkspaceRepository
import dev.sayaya.handbook.usecase.WorkspaceService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator

@Configuration
@EnableR2dbcAuditing
class WorkspaceConfig {
    /**
     * WorkspaceService 의 cascade delete 가 요구하는 reactive 트랜잭션 오퍼레이터.
     * Spring Boot R2DBC auto-config 가 제공하는 [ReactiveTransactionManager] 를 래핑한다.
     */
    @Bean
    fun workspaceTransactionalOperator(txManager: ReactiveTransactionManager): TransactionalOperator =
        TransactionalOperator.create(txManager)

    @Bean
    fun workspaceService(
        workspaceRepo: WorkspaceRepository,
        groupRepo: GroupRepository,
        webhookService: WebhookService,
        eventPublisher: WorkspaceEventPublisher,
        workspaceTransactionalOperator: TransactionalOperator,
    ) = WorkspaceService(workspaceRepo, groupRepo, webhookService, eventPublisher, workspaceTransactionalOperator)
}
