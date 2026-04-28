package dev.sayaya.handbook.interfaces.database

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.module.kotlin.KotlinModule
import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.DocumentPatch
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
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
    val repositoryFactory = R2dbcRepositoryFactory(template)
    val repo = repositoryFactory.getRepository(R2dbcDocumentEntityRepository::class.java)
    val objectMapper = JsonMapper.builder()
        .disable(DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .changeDefaultVisibility { it.withVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY) }
        .addModule(KotlinModule.Builder().withReflectionCacheSize(512).build())
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build()
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
                status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
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
                val existingRev = repo.findById(docId).block()!!.rev
                val updated = Document(
                    id = docId,
                    type = "invoice",
                    serial = "INV-001",
                    effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
                    expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
                    createDateTime = Instant.parse("2026-01-01T00:00:00Z"),
                    creator = "tester",
                    data = mapOf("title" to "Updated Invoice", "amount" to "2000"),
                    rev = existingRev,
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

        When("patchAll로 일부 필드만 변경하면") {
            Then("변경 필드만 업데이트되고 나머지는 유지된다") {
                adapter.saveAll(workspace, listOf(original)).collectList().block()
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

    Given("패치 버전 충돌") {
        val conflictDocId = UUID.randomUUID()
        val conflictDoc = Document(
            id = conflictDocId,
            type = "invoice",
            serial = "CONFLICT-001",
            effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
            createDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            creator = "tester",
            data = mapOf("name" to "충돌 테스트"),
        )

        When("잘못된 rev로 patchAll을 호출하면") {
            Then("DuplicateKeyException이 발생한다") {
                adapter.saveAll(workspace, listOf(conflictDoc)).collectList().block()
                val wrongRev = 999L
                val patch = DocumentPatch(
                    id = conflictDocId,
                    rev = wrongRev,
                    data = mapOf("name" to "변경된 이름"),
                )
                StepVerifier.create(adapter.patchAll(workspace, listOf(patch)))
                    .expectErrorMatches { it is org.springframework.dao.DuplicateKeyException && it.message!!.contains("Version conflict") }
                    .verify()
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

    Given("findAll 워크스페이스 필터") {
        val findAllWorkspace = UUID.randomUUID()
        val findAllDocs = listOf(
            Document(
                id = UUID.randomUUID(),
                type = "invoice",
                serial = "FA-001",
                effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
                expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
                createDateTime = Instant.parse("2026-01-01T00:00:00Z"),
                creator = "tester",
                data = mapOf("title" to "Find All Test 1"),
            ),
            Document(
                id = UUID.randomUUID(),
                type = "report",
                serial = "FA-002",
                effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
                expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
                createDateTime = Instant.parse("2026-01-01T00:00:00Z"),
                creator = "tester",
                data = mapOf("title" to "Find All Test 2"),
            ),
            Document(
                id = UUID.randomUUID(),
                type = "invoice",
                serial = "FA-003",
                effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
                expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
                createDateTime = Instant.parse("2026-01-01T00:00:00Z"),
                creator = "tester",
                data = mapOf("title" to "Find All Test 3"),
            ),
        )

        When("findAll을 워크스페이스로만 호출하면") {
            Then("해당 워크스페이스의 모든 문서가 반환된다") {
                adapter.saveAll(findAllWorkspace, findAllDocs).collectList().block()
                StepVerifier.create(adapter.findAll(findAllWorkspace, null).collectList())
                    .assertNext { list ->
                        list.size shouldBe 3
                    }
                    .verifyComplete()
            }
        }

        When("findAll을 type 필터와 함께 호출하면") {
            Then("해당 타입의 문서만 반환된다") {
                StepVerifier.create(adapter.findAll(findAllWorkspace, "invoice").collectList())
                    .assertNext { list ->
                        list.size shouldBe 2
                        list.all { it.type == "invoice" } shouldBe true
                    }
                    .verifyComplete()
            }
        }

        When("문서가 없는 워크스페이스로 findAll을 호출하면") {
            Then("빈 결과가 반환된다") {
                StepVerifier.create(adapter.findAll(UUID.randomUUID(), null))
                    .verifyComplete()
            }
        }
    }

    Given("findById") {
        val findByIdDocId = UUID.randomUUID()
        val findByIdDoc = Document(
            id = findByIdDocId,
            type = "invoice",
            serial = "FID-001",
            effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
            createDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            creator = "tester",
            data = mapOf("title" to "FindById Test"),
        )
        val findByIdWorkspace = UUID.randomUUID()

        When("존재하는 문서 ID로 findById를 호출하면") {
            Then("해당 문서가 반환된다") {
                adapter.saveAll(findByIdWorkspace, listOf(findByIdDoc)).collectList().block()
                StepVerifier.create(adapter.findById(findByIdDocId))
                    .assertNext { found ->
                        found.id shouldBe findByIdDocId
                        found.serial shouldBe "FID-001"
                        found.data["title"] shouldBe "FindById Test"
                    }
                    .verifyComplete()
            }
        }

        When("존재하지 않는 문서 ID로 findById를 호출하면") {
            Then("빈 결과가 반환된다") {
                StepVerifier.create(adapter.findById(UUID.randomUUID()))
                    .verifyComplete()
            }
        }
    }

    Given("updateStatus") {
        val statusDocId = UUID.randomUUID()
        val statusDoc = Document(
            id = statusDocId,
            type = "invoice",
            serial = "STS-001",
            effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
            createDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            creator = "tester",
            data = mapOf("title" to "Status Test"),
        )
        val statusWorkspace = UUID.randomUUID()

        When("유효한 문서의 상태를 변경하면") {
            Then("상태가 업데이트된 문서가 반환된다") {
                adapter.saveAll(statusWorkspace, listOf(statusDoc)).collectList().block()
                StepVerifier.create(adapter.updateStatus(statusDocId, "PUBLISHED"))
                    .assertNext { updated ->
                        updated.id shouldBe statusDocId
                        updated.status shouldBe "PUBLISHED"
                    }
                    .verifyComplete()
            }
        }

        When("존재하지 않는 문서의 상태를 변경하면") {
            Then("IllegalArgumentException이 발생한다") {
                StepVerifier.create(adapter.updateStatus(UUID.randomUUID(), "PUBLISHED"))
                    .expectErrorMatches { it is IllegalArgumentException && it.message!!.contains("Document not found") }
                    .verify()
            }
        }
    }
})
