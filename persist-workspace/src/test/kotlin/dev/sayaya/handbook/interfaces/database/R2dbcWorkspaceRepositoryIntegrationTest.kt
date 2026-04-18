package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Workspace
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.ConnectionFactoryOptions.*
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.DatabaseClient
import org.testcontainers.postgresql.PostgreSQLContainer
import reactor.test.StepVerifier
import java.util.*

class R2dbcWorkspaceRepositoryIntegrationTest : BehaviorSpec({
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
    val adapter = R2dbcWorkspaceRepositoryAdapter(template)

    beforeSpec {
        val client = DatabaseClient.create(connectionFactory)
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
    }

    afterSpec { postgres.stop() }

    Given("워크스페이스 생성") {
        val workspaceId = UUID.randomUUID()
        val creator = UUID.randomUUID()
        val workspace = Workspace(
            id = workspaceId,
            name = "테스트 워크스페이스",
            description = "통합 테스트용",
        )

        When("save를 호출하면") {
            Then("저장된 워크스페이스가 반환된다") {
                StepVerifier.create(adapter.save(workspace, creator))
                    .assertNext { saved ->
                        saved.id shouldBe workspaceId
                        saved.name shouldBe "테스트 워크스페이스"
                        saved.description shouldBe "통합 테스트용"
                    }
                    .verifyComplete()
            }

            Then("created_by / last_modified_by 에 명시 전달한 creator UUID 가 기록된다") {
                val createdBy = DatabaseClient.create(connectionFactory)
                    .sql("SELECT created_by FROM workspace WHERE id = :w")
                    .bind("w", workspaceId)
                    .map { row -> row.get("created_by", UUID::class.java) }
                    .one().block()
                val lastModifiedBy = DatabaseClient.create(connectionFactory)
                    .sql("SELECT last_modified_by FROM workspace WHERE id = :w")
                    .bind("w", workspaceId)
                    .map { row -> row.get("last_modified_by", UUID::class.java) }
                    .one().block()
                createdBy shouldBe creator
                lastModifiedBy shouldBe creator
            }
        }

        When("update를 호출하면") {
            Then("수정된 워크스페이스가 반환되고 last_modified_by 가 modifier 로 갱신된다") {
                val updated = Workspace(
                    id = workspaceId,
                    name = "수정된 워크스페이스",
                    description = "수정됨",
                )
                val modifier = UUID.randomUUID()
                StepVerifier.create(adapter.update(updated, modifier))
                    .assertNext { saved ->
                        saved.id shouldBe workspaceId
                        saved.name shouldBe "수정된 워크스페이스"
                        saved.description shouldBe "수정됨"
                    }
                    .verifyComplete()

                val createdBy = DatabaseClient.create(connectionFactory)
                    .sql("SELECT created_by FROM workspace WHERE id = :w")
                    .bind("w", workspaceId)
                    .map { row -> row.get("created_by", UUID::class.java) }
                    .one().block()
                val lastModifiedBy = DatabaseClient.create(connectionFactory)
                    .sql("SELECT last_modified_by FROM workspace WHERE id = :w")
                    .bind("w", workspaceId)
                    .map { row -> row.get("last_modified_by", UUID::class.java) }
                    .one().block()
                // update 는 last_modified_by 만 바꾼다 — created_by 는 원본 creator 유지.
                createdBy shouldBe creator
                lastModifiedBy shouldBe modifier
            }
        }

        When("delete를 호출하면") {
            Then("워크스페이스가 삭제된다") {
                StepVerifier.create(adapter.delete(workspaceId))
                    .verifyComplete()

                // 삭제 후 update 시도 시 빈 결과
                val deleted = Workspace(
                    id = workspaceId,
                    name = "삭제된 워크스페이스",
                    description = null,
                )
                StepVerifier.create(adapter.update(deleted, UUID.randomUUID()))
                    .verifyComplete()
            }
        }
    }

    Given("여러 워크스페이스 생성") {
        val workspaces = (1..3).map { i ->
            Workspace(
                id = UUID.randomUUID(),
                name = "워크스페이스-$i",
                description = "설명-$i",
            )
        }

        When("각각 save를 호출하면") {
            Then("모두 저장된다") {
                workspaces.forEach { ws ->
                    StepVerifier.create(adapter.save(ws, UUID.randomUUID()))
                        .assertNext { saved ->
                            saved.id shouldBe ws.id
                            saved.name shouldBe ws.name
                        }
                        .verifyComplete()
                }
            }
        }
    }
})
