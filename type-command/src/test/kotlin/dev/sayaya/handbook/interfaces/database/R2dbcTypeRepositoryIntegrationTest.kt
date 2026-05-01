package dev.sayaya.handbook.interfaces.database

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.AttributeType
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.domain.TypePatch
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.repository.support.R2dbcRepositoryFactory
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.transaction.reactive.TransactionalOperator
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

class R2dbcTypeRepositoryIntegrationTest : BehaviorSpec({
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
    val typeRepo = repositoryFactory.getRepository(R2dbcTypeEntityRepository::class.java)
    val attrRepo = repositoryFactory.getRepository(R2dbcAttributeEntityRepository::class.java)
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
    val attrMapper = AttributeEntityMapper(objectMapper)
    val adapter = R2dbcTypeRepositoryAdapter(typeRepo, attrRepo, attrMapper, tx, databaseClient)

    val workspace = UUID.randomUUID()

    beforeSpec {
        val client = DatabaseClient.create(connectionFactory)
        client.sql("""
            CREATE TABLE types (
                id VARCHAR(255) NOT NULL,
                version VARCHAR(255) NOT NULL,
                workspace UUID NOT NULL,
                effect_date_time TIMESTAMPTZ NOT NULL,
                expire_date_time TIMESTAMPTZ NOT NULL,
                description TEXT,
                primitive BOOLEAN NOT NULL DEFAULT FALSE,
                parent VARCHAR(255),
                rev BIGINT,
                PRIMARY KEY (id, version, workspace)
            )
        """).then().block()
        client.sql("""
            CREATE TABLE type_attributes (
                id UUID DEFAULT gen_random_uuid() NOT NULL,
                type_id VARCHAR(255) NOT NULL,
                type_version VARCHAR(255) NOT NULL,
                workspace UUID NOT NULL,
                name VARCHAR(255) NOT NULL,
                attr_order SMALLINT NOT NULL,
                description TEXT,
                attribute_type JSONB NOT NULL,
                nullable BOOLEAN NOT NULL DEFAULT FALSE,
                inherited BOOLEAN NOT NULL DEFAULT FALSE,
                read_roles JSONB NOT NULL DEFAULT '[]'::jsonb,
                write_roles JSONB NOT NULL DEFAULT '[]'::jsonb,
                PRIMARY KEY (id)
            )
        """).then().block()
    }

    afterSpec { postgres.stop() }

    Given("타입 저장") {
        val type = Type(
            id = "customer",
            version = "1.0",
            effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
            expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
            description = "고객 타입",
            primitive = false,
            attributes = listOf(
                Attribute(
                    name = "name",
                    order = 0,
                    description = "고객 이름",
                    type = AttributeType.Text(),
                    nullable = false,
                    inherited = false,
                ),
                Attribute(
                    name = "age",
                    order = 1,
                    description = "나이",
                    type = AttributeType.Number(min = 0, max = 200),
                    nullable = true,
                    inherited = false,
                ),
            ),
        )

        When("save를 호출하면") {
            Then("저장된 타입이 반환된다") {
                StepVerifier.create(adapter.save(workspace, listOf(type)))
                    .assertNext { saved ->
                        saved.id shouldBe "customer"
                        saved.version shouldBe "1.0"
                        saved.description shouldBe "고객 타입"
                        saved.primitive shouldBe false
                    }
                    .verifyComplete()
            }
        }

        When("findByWorkspaceAndPeriod로 조회하면") {
            Then("저장된 타입을 찾을 수 있다") {
                StepVerifier.create(
                    adapter.findByWorkspaceAndPeriod(
                        workspace,
                        Instant.parse("2026-06-01T00:00:00Z"),
                        Instant.parse("2026-06-30T23:59:59Z"),
                    )
                )
                    .assertNext { found ->
                        found.id shouldBe "customer"
                        found.version shouldBe "1.0"
                        found.attributes.size shouldBe 2
                        found.attributes[0].name shouldBe "name"
                        found.attributes[1].name shouldBe "age"
                    }
                    .verifyComplete()
            }
        }

        When("범위 밖의 기간으로 조회하면") {
            Then("결과가 비어 있다") {
                StepVerifier.create(
                    adapter.findByWorkspaceAndPeriod(
                        workspace,
                        Instant.parse("2027-01-01T00:00:00Z"),
                        Instant.parse("2027-12-31T23:59:59Z"),
                    )
                ).verifyComplete()
            }
        }

        When("patch로 일부 속성만 변경하면") {
            Then("변경 속성만 업데이트되고 나머지는 유지된다") {
                val savedRev = typeRepo.findById("customer").block()!!.rev!!

                val patch = TypePatch(
                    id = "customer",
                    version = "1.0",
                    rev = savedRev,
                    attributes = listOf(
                        Attribute(
                            name = "phone",
                            order = 2,
                            description = "전화번호",
                            type = AttributeType.Text(),
                            nullable = true,
                            inherited = false,
                        ),
                    ),
                )
                StepVerifier.create(adapter.patch(workspace, listOf(patch)))
                    .assertNext { patched ->
                        patched.id shouldBe "customer"
                        patched.attributes.size shouldBe 3
                        patched.attributes.any { it.name == "name" } shouldBe true
                        patched.attributes.any { it.name == "age" } shouldBe true
                        patched.attributes.any { it.name == "phone" } shouldBe true
                    }
                    .verifyComplete()
            }
        }

        When("잘못된 rev로 patch를 호출하면") {
            Then("DuplicateKeyException이 발생한다") {
                val wrongRev = 999L
                val patch = TypePatch(
                    id = "customer",
                    version = "1.0",
                    rev = wrongRev,
                    attributes = listOf(
                        Attribute(
                            name = "email",
                            order = 3,
                            description = "이메일",
                            type = AttributeType.Text(),
                            nullable = true,
                            inherited = false,
                        ),
                    ),
                )
                StepVerifier.create(adapter.patch(workspace, listOf(patch)))
                    .expectErrorMatches { it is org.springframework.dao.DuplicateKeyException && it.message!!.contains("Version conflict") }
                    .verify()
            }
        }

        When("delete를 호출하면") {
            Then("타입이 삭제된다") {
                StepVerifier.create(adapter.delete(workspace, listOf(type)))
                    .verifyComplete()

                StepVerifier.create(
                    adapter.findByWorkspaceAndPeriod(
                        workspace,
                        Instant.parse("2026-06-01T00:00:00Z"),
                        Instant.parse("2026-06-30T23:59:59Z"),
                    )
                ).verifyComplete()
            }
        }
    }
})
