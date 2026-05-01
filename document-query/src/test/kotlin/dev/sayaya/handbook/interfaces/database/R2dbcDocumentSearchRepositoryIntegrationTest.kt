package dev.sayaya.handbook.interfaces.database

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import dev.sayaya.handbook.domain.Search
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.DatabaseClient
import org.testcontainers.postgresql.PostgreSQLContainer
import reactor.test.StepVerifier
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import java.time.Instant
import java.util.*

class R2dbcDocumentSearchRepositoryIntegrationTest : BehaviorSpec({
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

    val template = R2dbcEntityTemplate(connectionFactory)
    val objectMapper = JsonMapper.builder()
        .disable(DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .changeDefaultVisibility { it.withVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY) }
        .addModule(KotlinModule.Builder().withReflectionCacheSize(512).build())
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build()
    val repository = R2dbcDocumentSearchRepository(template, objectMapper)
    val databaseClient = DatabaseClient.create(connectionFactory)

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

        // 테스트 데이터 삽입
        val docs = listOf(
            Triple("invoice", "INV-001", """{"title":"Invoice 1"}"""),
            Triple("invoice", "INV-002", """{"title":"Invoice 2"}"""),
            Triple("invoice", "INV-003", """{"title":"Invoice 3"}"""),
            Triple("report", "RPT-001", """{"title":"Report 1"}"""),
            Triple("report", "RPT-002", """{"title":"Report 2"}"""),
        )
        docs.forEach { (type, serial, data) ->
            databaseClient.sql("""
                INSERT INTO documents (workspace, type, serial, effect_date_time, expire_date_time, data, create_date_time, creator)
                VALUES (:workspace, :type, :serial, :effect, :expire, :data::jsonb, :createTime, 'tester')
            """)
                .bind("workspace", workspace)
                .bind("type", type)
                .bind("serial", serial)
                .bind("effect", Instant.parse("2026-01-01T00:00:00Z"))
                .bind("expire", Instant.parse("2027-01-01T00:00:00Z"))
                .bind("data", data)
                .bind("createTime", Instant.now())
                .then().block()
        }

        // 다른 워크스페이스 문서
        databaseClient.sql("""
            INSERT INTO documents (workspace, type, serial, effect_date_time, expire_date_time, create_date_time, creator)
            VALUES (:workspace, 'memo', 'MEM-001', '2026-01-01T00:00:00Z', '2027-01-01T00:00:00Z', NOW(), 'other')
        """)
            .bind("workspace", otherWorkspace)
            .then().block()
    }

    afterSpec { postgres.stop() }

    Given("문서 검색 (search)") {
        When("워크스페이스 내 전체 검색을 요청하면") {
            Then("해당 워크스페이스의 문서 페이지가 반환된다") {
                val param = Search(page = 0, limit = 10, sortBy = null, asc = null)
                StepVerifier.create(repository.search(workspace, param))
                    .assertNext { page ->
                        page.totalElements shouldBe 5L
                        page.content.size shouldBe 5
                    }
                    .verifyComplete()
            }
        }

        When("페이지네이션을 적용하면") {
            Then("지정된 크기만큼 반환된다") {
                val param = Search(page = 0, limit = 2, sortBy = "serial", asc = true)
                StepVerifier.create(repository.search(workspace, param))
                    .assertNext { page ->
                        page.totalElements shouldBe 5L
                        page.content.size shouldBe 2
                        page.content[0].serial shouldBe "INV-001"
                        page.content[1].serial shouldBe "INV-002"
                    }
                    .verifyComplete()
            }
        }

        When("두 번째 페이지를 요청하면") {
            Then("나머지 문서가 반환된다") {
                val param = Search(page = 1, limit = 2, sortBy = "serial", asc = true)
                StepVerifier.create(repository.search(workspace, param))
                    .assertNext { page ->
                        page.totalElements shouldBe 5L
                        page.content.size shouldBe 2
                        page.content[0].serial shouldBe "INV-003"
                        page.content[1].serial shouldBe "RPT-001"
                    }
                    .verifyComplete()
            }
        }

        When("type 필터를 적용하면") {
            Then("해당 타입의 문서만 반환된다") {
                val param = Search(
                    page = 0, limit = 10, sortBy = null, asc = null,
                    filters = listOf("type" to "report"),
                )
                StepVerifier.create(repository.search(workspace, param))
                    .assertNext { page ->
                        page.totalElements shouldBe 2L
                        page.content.all { it.type == "report" } shouldBe true
                    }
                    .verifyComplete()
            }
        }

        When("serial 필터를 적용하면") {
            Then("해당 serial의 문서만 반환된다") {
                val param = Search(
                    page = 0, limit = 10, sortBy = null, asc = null,
                    filters = listOf("serial" to "INV-001"),
                )
                StepVerifier.create(repository.search(workspace, param))
                    .assertNext { page ->
                        page.totalElements shouldBe 1L
                        page.content[0].serial shouldBe "INV-001"
                    }
                    .verifyComplete()
            }
        }

        When("내림차순 정렬을 적용하면") {
            Then("역순으로 반환된다") {
                val param = Search(page = 0, limit = 3, sortBy = "serial", asc = false)
                StepVerifier.create(repository.search(workspace, param))
                    .assertNext { page ->
                        page.content[0].serial shouldBe "RPT-002"
                        page.content[1].serial shouldBe "RPT-001"
                        page.content[2].serial shouldBe "INV-003"
                    }
                    .verifyComplete()
            }
        }

        When("다른 워크스페이스의 문서는 포함되지 않는다") {
            Then("해당 워크스페이스 문서만 반환된다") {
                val param = Search(page = 0, limit = 10, sortBy = null, asc = null)
                StepVerifier.create(repository.search(otherWorkspace, param))
                    .assertNext { page ->
                        page.totalElements shouldBe 1L
                        page.content[0].type shouldBe "memo"
                    }
                    .verifyComplete()
            }
        }
    }

    Given("특정 문서 조회 (find)") {
        When("유효한 workspace, type, serial, date로 find를 호출하면") {
            Then("해당 문서가 반환된다") {
                val date = Instant.parse("2026-06-15T00:00:00Z")
                StepVerifier.create(repository.find(workspace, "invoice", "INV-001", date))
                    .assertNext { doc ->
                        doc.serial shouldBe "INV-001"
                        doc.type shouldBe "invoice"
                        doc.data["title"] shouldBe "Invoice 1"
                    }
                    .verifyComplete()
            }
        }

        When("유효 기간 밖의 날짜로 find를 호출하면") {
            Then("빈 결과가 반환된다") {
                val date = Instant.parse("2028-01-01T00:00:00Z")
                StepVerifier.create(repository.find(workspace, "invoice", "INV-001", date))
                    .verifyComplete()
            }
        }

        When("존재하지 않는 serial로 find를 호출하면") {
            Then("빈 결과가 반환된다") {
                val date = Instant.parse("2026-06-15T00:00:00Z")
                StepVerifier.create(repository.find(workspace, "invoice", "NO-EXIST", date))
                    .verifyComplete()
            }
        }

        When("다른 워크스페이스로 find를 호출하면") {
            Then("빈 결과가 반환된다") {
                val date = Instant.parse("2026-06-15T00:00:00Z")
                StepVerifier.create(repository.find(otherWorkspace, "invoice", "INV-001", date))
                    .verifyComplete()
            }
        }
    }
})
