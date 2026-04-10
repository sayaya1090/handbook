package dev.sayaya.handbook.interfaces.database

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.DocumentPatch
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.ConnectionFactoryOptions.*
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.repository.support.R2dbcRepositoryFactory
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.transaction.reactive.TransactionalOperator
import org.testcontainers.postgresql.PostgreSQLContainer
import reactor.test.StepVerifier
import java.time.Instant
import java.util.*

class R2dbcDocumentRepositoryIntegrationTest : BehaviorSpec({
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
    val repositoryFactory = R2dbcRepositoryFactory(template)
    val repo = repositoryFactory.getRepository(R2dbcDocumentEntityRepository::class.java)
    val objectMapper = ObjectMapper()
        .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .registerModule(JavaTimeModule())
        .registerModule(KotlinModule.Builder().withReflectionCacheSize(512).build())
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    val tx = TransactionalOperator.create(R2dbcTransactionManager(connectionFactory))
    val databaseClient = DatabaseClient.create(connectionFactory)
    val adapter = R2dbcDocumentRepositoryAdapter(repo, objectMapper, tx, databaseClient)

    val workspace = UUID.randomUUID()

    beforeSpec {
        val client = DatabaseClient.create(connectionFactory)
        client.sql("""
            CREATE TABLE documents (
                id UUID NOT NULL DEFAULT gen_random_uuid(),
                workspace UUID NOT NULL,
                type VARCHAR(255) NOT NULL,
                serial VARCHAR(255) NOT NULL,
                effect_date_time TIMESTAMPTZ NOT NULL,
                expire_date_time TIMESTAMPTZ NOT NULL,
                data JSONB NOT NULL DEFAULT '{}'::jsonb,
                create_date_time TIMESTAMPTZ,
                creator VARCHAR(255),
                rev BIGINT,
                PRIMARY KEY (id)
            )
        """).then().block()
    }

    afterSpec { postgres.stop() }

    Given("문서 저장 및 삭제") {
        val docId = UUID.randomUUID()
        val document = Document(
            id = docId,
            type = "invoice",
            serial = "INV-001",
            effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
            createDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            creator = "tester",
            data = mapOf("title" to "Test Invoice", "amount" to "1000"),
        )

        When("saveAll을 호출하면") {
            Then("저장된 문서가 반환된다") {
                StepVerifier.create(adapter.saveAll(workspace, listOf(document)))
                    .assertNext { saved ->
                        saved.id shouldNotBe null
                        saved.type shouldBe "invoice"
                        saved.serial shouldBe "INV-001"
                        saved.data["title"] shouldBe "Test Invoice"
                        saved.data["amount"] shouldBe "1000"
                    }
                    .verifyComplete()
            }
        }

        When("동일 문서를 다시 saveAll하면") {
            Then("업데이트된 문서가 반환된다") {
                val updated = Document(
                    id = docId,
                    type = "invoice",
                    serial = "INV-001",
                    effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
                    expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
                    createDateTime = Instant.parse("2026-01-01T00:00:00Z"),
                    creator = "tester",
                    data = mapOf("title" to "Updated Invoice", "amount" to "2000"),
                )
                StepVerifier.create(adapter.saveAll(workspace, listOf(updated)))
                    .assertNext { saved ->
                        saved.id shouldBe docId
                        saved.data["title"] shouldBe "Updated Invoice"
                    }
                    .verifyComplete()
            }
        }

        When("deleteAll을 호출하면") {
            Then("문서가 삭제된다") {
                StepVerifier.create(adapter.deleteAll(workspace, listOf(document)))
                    .verifyComplete()

                // 삭제 후 조회해서 없는지 확인
                StepVerifier.create(repo.findById(docId))
                    .verifyComplete()
            }
        }
    }

    Given("패치 기반 부분 업데이트") {
        val patchDocId = UUID.randomUUID()
        val original = Document(
            id = patchDocId,
            type = "invoice",
            serial = "PATCH-001",
            effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
            createDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            creator = "tester",
            data = mapOf("name" to "홍길동", "phone" to "010-1234"),
        )

        beforeTest {
            adapter.saveAll(workspace, listOf(original)).collectList().block()
        }

        When("patchAll로 일부 필드만 변경하면") {
            Then("변경 필드만 업데이트되고 나머지는 유지된다") {
                val savedRev = repo.findById(patchDocId).block()!!.rev!!
                val patch = DocumentPatch(
                    id = patchDocId,
                    rev = savedRev,
                    data = mapOf("phone" to "010-5678"),
                )
                StepVerifier.create(adapter.patchAll(workspace, listOf(patch)))
                    .assertNext { patched ->
                        patched.data["name"] shouldBe "홍길동"
                        patched.data["phone"] shouldBe "010-5678"
                    }
                    .verifyComplete()
            }
        }
    }

    Given("여러 문서 동시 저장") {
        val docs = (1..3).map { i ->
            Document(
                id = UUID.randomUUID(),
                type = "report",
                serial = "RPT-00$i",
                effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
                expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
                createDateTime = Instant.parse("2026-01-01T00:00:00Z"),
                creator = "tester",
                data = mapOf("index" to "$i"),
            )
        }

        When("saveAll로 여러 문서를 저장하면") {
            Then("모든 문서가 저장된다") {
                StepVerifier.create(adapter.saveAll(workspace, docs))
                    .expectNextCount(3)
                    .verifyComplete()
            }
        }
    }
})
