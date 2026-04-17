package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Workspace
import dev.sayaya.handbook.usecase.WebhookService
import dev.sayaya.handbook.usecase.WorkspaceService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.ConnectionFactoryOptions.DATABASE
import io.r2dbc.spi.ConnectionFactoryOptions.DRIVER
import io.r2dbc.spi.ConnectionFactoryOptions.HOST
import io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD
import io.r2dbc.spi.ConnectionFactoryOptions.PORT
import io.r2dbc.spi.ConnectionFactoryOptions.USER
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.transaction.reactive.TransactionalOperator
import org.testcontainers.postgresql.PostgreSQLContainer
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.security.Principal
import java.util.UUID

/**
 * 워크스페이스 cascade 삭제 통합 테스트.
 *
 * `WorkspaceService.delete` 가 하나의 R2DBC 트랜잭션에서 웹훅 → 그룹/멤버 → 워크스페이스
 * 순서로 삭제하는지, 다른 워크스페이스의 row 는 보존되는지 검증한다.
 *
 * `R2dbcWebhookRepository` 는 ReactiveCrudRepository 자동 구현이 필요해 Spring context
 * 가 필요하므로 이 테스트에서는 WebhookService 를 mock 으로 대체하고, 실제 DB 에 대한
 * cascade 검증은 `group` 쪽(`R2dbcGroupRepositoryAdapter`)에 집중한다. 웹훅 쪽 cascade
 * 는 단위 테스트(WorkspaceServiceTest)의 verifyOrder 로 커버된다.
 */
class R2dbcWorkspaceCascadeIntegrationTest : BehaviorSpec({
    val postgres = PostgreSQLContainer("postgres:17").apply { start() }

    val connectionFactory = ConnectionFactories.get(
        ConnectionFactoryOptions.builder()
            .option(DRIVER, "postgresql")
            .option(HOST, postgres.host)
            .option(PORT, postgres.firstMappedPort)
            .option(DATABASE, postgres.databaseName)
            .option(USER, postgres.username)
            .option(PASSWORD, postgres.password)
            .build()
    )

    val template = R2dbcEntityTemplate(connectionFactory)
    val client = DatabaseClient.create(connectionFactory)
    val tx = TransactionalOperator.create(R2dbcTransactionManager(connectionFactory))

    val workspaceRepo = R2dbcWorkspaceRepositoryAdapter(template)
    val groupRepo = R2dbcGroupRepositoryAdapter(template)
    val webhookService = mockk<WebhookService>()
    val eventPublisher = mockk<dev.sayaya.handbook.usecase.WorkspaceEventPublisher>()

    val service = WorkspaceService(workspaceRepo, groupRepo, webhookService, eventPublisher, tx)

    val keep = UUID.randomUUID()
    val target = UUID.randomUUID()

    beforeSpec {
        client.sql("""
            CREATE TABLE workspace (
                id UUID NOT NULL,
                name VARCHAR(255) NOT NULL,
                description TEXT,
                version BIGINT,
                created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                created_by UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
                last_modified_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                last_modified_by UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
                PRIMARY KEY (id)
            )
        """).then().block()
        client.sql("""
            CREATE TABLE "group" (
                workspace UUID NOT NULL,
                name VARCHAR(255) NOT NULL,
                created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                created_by UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
                PRIMARY KEY (workspace, name)
            )
        """).then().block()
        client.sql("""
            CREATE TABLE group_member (
                workspace UUID NOT NULL,
                "group" VARCHAR(255) NOT NULL,
                member UUID NOT NULL,
                PRIMARY KEY (workspace, "group", member)
            )
        """).then().block()

        // 두 워크스페이스 씨드
        listOf(keep to "keeper", target to "doomed").forEach { (id, label) ->
            workspaceRepo.save(Workspace(id, label, null)).block()
            val principal = Principal { UUID.randomUUID().toString() }
            groupRepo.createAndAssign(Workspace(id, label, null), principal, "Admin", null).block()
        }
    }

    afterSpec { postgres.stop() }

    Given("두 워크스페이스 각각에 Admin 그룹 + 1 멤버가 있는 상태") {
        When("target 워크스페이스에 delete 를 호출하면") {
            every { webhookService.deleteByWorkspace(target) } returns Mono.empty()
            every { eventPublisher.publishDeleted(target) } returns Mono.empty()

            Then("cascade 로 target 의 그룹·멤버·워크스페이스 row 가 모두 삭제된다") {
                StepVerifier.create(service.delete(target))
                    .verifyComplete()

                val remainingGroups = client.sql("SELECT COUNT(*) FROM \"group\" WHERE workspace = :w")
                    .bind("w", target)
                    .map { row -> row.get(0, java.lang.Long::class.java)?.toLong() ?: 0L }
                    .one().block()
                val remainingMembers = client.sql("SELECT COUNT(*) FROM group_member WHERE workspace = :w")
                    .bind("w", target)
                    .map { row -> row.get(0, java.lang.Long::class.java)?.toLong() ?: 0L }
                    .one().block()
                val remainingWorkspace = client.sql("SELECT COUNT(*) FROM workspace WHERE id = :w")
                    .bind("w", target)
                    .map { row -> row.get(0, java.lang.Long::class.java)?.toLong() ?: 0L }
                    .one().block()

                remainingGroups shouldBe 0L
                remainingMembers shouldBe 0L
                remainingWorkspace shouldBe 0L
            }

            Then("keep 워크스페이스의 그룹·멤버·워크스페이스 row 는 보존된다") {
                val keptGroups = client.sql("SELECT COUNT(*) FROM \"group\" WHERE workspace = :w")
                    .bind("w", keep)
                    .map { row -> row.get(0, java.lang.Long::class.java)?.toLong() ?: 0L }
                    .one().block()
                val keptMembers = client.sql("SELECT COUNT(*) FROM group_member WHERE workspace = :w")
                    .bind("w", keep)
                    .map { row -> row.get(0, java.lang.Long::class.java)?.toLong() ?: 0L }
                    .one().block()
                val keptWorkspace = client.sql("SELECT COUNT(*) FROM workspace WHERE id = :w")
                    .bind("w", keep)
                    .map { row -> row.get(0, java.lang.Long::class.java)?.toLong() ?: 0L }
                    .one().block()

                keptGroups shouldBe 1L
                keptMembers shouldBe 1L
                keptWorkspace shouldBe 1L
            }
        }
    }
})
