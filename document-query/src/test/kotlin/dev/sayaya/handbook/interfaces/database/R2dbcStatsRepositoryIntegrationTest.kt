package dev.sayaya.handbook.interfaces.database

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import org.springframework.r2dbc.core.DatabaseClient
import org.testcontainers.postgresql.PostgreSQLContainer
import reactor.test.StepVerifier
import java.time.Instant
import java.util.*

class R2dbcStatsRepositoryIntegrationTest : BehaviorSpec({
    val postgres = PostgreSQLContainer("postgres:17").apply { start() }

    val connectionFactory = PostgresqlConnectionFactory(
        PostgresqlConnectionConfiguration.builder()
            .host(postgres.host)
            .port(postgres.firstMappedPort)
            .database(postgres.databaseName)
            .username(postgres.username)
            .password(postgres.password)
            .build()
    )

    val databaseClient = DatabaseClient.create(connectionFactory)
    val repository = R2dbcStatsRepository(databaseClient)
    val workspace = UUID.randomUUID()
    val otherWorkspace = UUID.randomUUID()

    beforeSpec {
        databaseClient.sql("""
            CREATE TABLE documents (
                id UUID NOT NULL DEFAULT gen_random_uuid(),
                workspace UUID NOT NULL,
                type VARCHAR(255) NOT NULL,
                serial VARCHAR(255) NOT NULL,
                effect_date_time TIMESTAMPTZ NOT NULL,
                expire_date_time TIMESTAMPTZ NOT NULL,
                data JSONB NOT NULL DEFAULT '{}'::jsonb,
                status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
                create_date_time TIMESTAMPTZ,
                creator VARCHAR(255),
                rev BIGINT,
                PRIMARY KEY (id)
            )
        """).then().block()

        // 테스트 데이터 삽입: 다양한 날짜와 타입
        val docs = listOf(
            Triple("invoice", "INV-001", "2026-03-01T01:00:00Z"),
            Triple("invoice", "INV-002", "2026-03-01T02:00:00Z"),
            Triple("invoice", "INV-003", "2026-03-15T01:00:00Z"),
            Triple("report", "RPT-001", "2026-03-15T02:00:00Z"),
            Triple("report", "RPT-002", "2026-04-01T01:00:00Z"),
            Triple("contract", "CTR-001", "2026-04-01T02:00:00Z"),
        )
        docs.forEach { (type, serial, createTime) ->
            databaseClient.sql("""
                INSERT INTO documents (workspace, type, serial, effect_date_time, expire_date_time, create_date_time, creator)
                VALUES (:workspace, :type, :serial, :effect, :expire, :createTime, 'tester')
            """)
                .bind("workspace", workspace)
                .bind("type", type)
                .bind("serial", serial)
                .bind("effect", Instant.parse("2026-01-01T00:00:00Z"))
                .bind("expire", Instant.parse("2027-01-01T00:00:00Z"))
                .bind("createTime", Instant.parse(createTime))
                .then().block()
        }

        // 다른 워크스페이스 데이터 (격리 확인용)
        databaseClient.sql("""
            INSERT INTO documents (workspace, type, serial, effect_date_time, expire_date_time, create_date_time, creator)
            VALUES (:workspace, 'memo', 'MEM-001', '2026-03-01T00:00:00Z', '2027-01-01T00:00:00Z', '2026-03-01T10:00:00Z', 'other')
        """)
            .bind("workspace", otherWorkspace)
            .then().block()
    }

    afterSpec { postgres.stop() }

    Given("타임라인 조회") {
        When("유효한 기간으로 timeline을 호출하면") {
            Then("날짜별 문서 수가 반환된다") {
                val result = repository.timeline(
                    workspace,
                    Instant.parse("2026-03-01T00:00:00Z"),
                    Instant.parse("2026-04-02T00:00:00Z"),
                    1,
                )
                StepVerifier.create(result.collectList())
                    .assertNext { entries ->
                        entries.size shouldBeGreaterThanOrEqual 3
                        // 6개 문서가 3개 이상의 날짜 버킷으로 분배됨을 검증
                        val totalCount = entries.sumOf { it.documentCount }
                        totalCount shouldBe 6L
                    }
                    .verifyComplete()
            }
        }

        When("문서가 없는 기간으로 timeline을 호출하면") {
            Then("빈 결과가 반환된다") {
                val result = repository.timeline(
                    workspace,
                    Instant.parse("2027-01-01T00:00:00Z"),
                    Instant.parse("2027-12-31T00:00:00Z"),
                    1,
                )
                StepVerifier.create(result)
                    .verifyComplete()
            }
        }

        When("다른 워크스페이스의 데이터는 포함되지 않는다") {
            Then("해당 워크스페이스 문서만 집계된다") {
                val result = repository.timeline(
                    otherWorkspace,
                    Instant.parse("2026-03-01T00:00:00Z"),
                    Instant.parse("2026-04-02T00:00:00Z"),
                    1,
                )
                StepVerifier.create(result.collectList())
                    .assertNext { entries ->
                        entries.size shouldBe 1
                        entries[0].documentCount shouldBe 1L
                    }
                    .verifyComplete()
            }
        }
    }

    Given("분포 조회") {
        When("distribution을 호출하면") {
            Then("타입별 문서 수가 내림차순으로 반환된다") {
                val result = repository.distribution(workspace)
                StepVerifier.create(result.collectList())
                    .assertNext { entries ->
                        entries.size shouldBe 3
                        // invoice=3, report=2, contract=1 (내림차순)
                        entries[0].type shouldBe "invoice"
                        entries[0].count shouldBe 3L
                        entries[1].type shouldBe "report"
                        entries[1].count shouldBe 2L
                        entries[2].type shouldBe "contract"
                        entries[2].count shouldBe 1L
                    }
                    .verifyComplete()
            }
        }

        When("다른 워크스페이스의 distribution을 호출하면") {
            Then("해당 워크스페이스 문서만 집계된다") {
                val result = repository.distribution(otherWorkspace)
                StepVerifier.create(result.collectList())
                    .assertNext { entries ->
                        entries.size shouldBe 1
                        entries[0].type shouldBe "memo"
                        entries[0].count shouldBe 1L
                    }
                    .verifyComplete()
            }
        }

        When("문서가 없는 워크스페이스로 distribution을 호출하면") {
            Then("빈 결과가 반환된다") {
                val result = repository.distribution(UUID.randomUUID())
                StepVerifier.create(result)
                    .verifyComplete()
            }
        }
    }
})
