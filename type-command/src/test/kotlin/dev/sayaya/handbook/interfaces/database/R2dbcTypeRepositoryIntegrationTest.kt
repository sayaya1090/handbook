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
        client.sql("""
            CREATE OR REPLACE FUNCTION enforce_no_overlap_type_periods() RETURNS TRIGGER AS $$
            BEGIN
                IF EXISTS (
                    SELECT 1 FROM types
                    WHERE workspace = NEW.workspace AND id = NEW.id AND version <> NEW.version
                      AND (NEW.effect_date_time, NEW.expire_date_time) OVERLAPS (effect_date_time, expire_date_time)
                ) THEN
                    RAISE EXCEPTION 'Overlapping periods are not allowed for type id: %, version: %, effect: %, expire: %', NEW.id, NEW.version, NEW.effect_date_time, NEW.expire_date_time;
                END IF;
                RETURN NEW;
            END;
            $$ LANGUAGE plpgsql;
            CREATE CONSTRAINT TRIGGER enforce_no_overlap_type_periods_trigger AFTER INSERT OR UPDATE ON types DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION enforce_no_overlap_type_periods();

            CREATE OR REPLACE FUNCTION enforce_parent_type_consistency() RETURNS TRIGGER AS $$
            BEGIN
                IF (NEW.parent IS NOT NULL AND NEW.parent <> '') THEN
                    IF NOT EXISTS (
                        SELECT 1 FROM types WHERE workspace = NEW.workspace AND id = NEW.parent
                          AND (effect_date_time, expire_date_time) OVERLAPS (NEW.effect_date_time, NEW.expire_date_time)
                    ) THEN
                        RAISE EXCEPTION 'Parent type (id=%) does not exist during the effective period.', NEW.parent;
                    END IF;
                END IF;
                RETURN NEW;
            END;
            $$ LANGUAGE plpgsql;
            CREATE CONSTRAINT TRIGGER enforce_parent_type_consistency_trigger AFTER INSERT OR UPDATE ON types DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION enforce_parent_type_consistency();

            CREATE OR REPLACE FUNCTION enforce_attribute_reference_consistency() RETURNS TRIGGER AS $$
            DECLARE
                owner_effect TIMESTAMPTZ;
                owner_expire TIMESTAMPTZ;
                ref_type_id  VARCHAR(255);
            BEGIN
                ref_type_id := NEW.attribute_type ->> 'referenced_type';
                IF (ref_type_id IS NULL) THEN RETURN NEW; END IF;
                
                SELECT effect_date_time, expire_date_time INTO owner_effect, owner_expire FROM types
                WHERE workspace = NEW.workspace AND id = NEW.type_id AND version = NEW.type_version;
                
                IF owner_effect IS NULL THEN
                    RAISE EXCEPTION 'Owner type (id=%, version=%) not found in types table.', NEW.type_id, NEW.type_version;
                END IF;

                IF NOT EXISTS (
                    SELECT 1 FROM types WHERE workspace = NEW.workspace AND id = ref_type_id
                      AND (effect_date_time, expire_date_time) OVERLAPS (owner_effect, owner_expire)
                ) THEN
                    RAISE EXCEPTION 'Referenced type (id=%) does not exist during the owner type period.', ref_type_id;
                END IF;
                
                IF EXISTS (
                    SELECT 1 FROM (
                             SELECT MIN(effect_date_time) AS combined_start, MAX(expire_date_time) AS combined_end,
                                 SUM(CASE WHEN previous_expire_date_time IS NOT NULL AND previous_expire_date_time <> effect_date_time THEN 1 ELSE 0 END) AS gaps
                             FROM (
                                      SELECT effect_date_time, expire_date_time, LAG(expire_date_time) OVER (ORDER BY effect_date_time) AS previous_expire_date_time
                                      FROM types WHERE workspace = NEW.workspace AND id = ref_type_id
                                        AND (effect_date_time, expire_date_time) OVERLAPS (owner_effect, owner_expire)
                                  ) sub
                         ) merged
                    WHERE gaps > 0 OR combined_start > owner_effect OR combined_end < owner_expire
                ) THEN
                    RAISE EXCEPTION 'Referenced type (id=%) has gaps or does not fully cover the owner type period [%, %]', ref_type_id, owner_effect, owner_expire;
                END IF;
                RETURN NEW;
            END;
            $$ LANGUAGE plpgsql;
            CREATE CONSTRAINT TRIGGER enforce_attribute_reference_consistency_trigger AFTER INSERT OR UPDATE ON type_attributes FOR EACH ROW EXECUTE FUNCTION enforce_attribute_reference_consistency();

            CREATE OR REPLACE FUNCTION prevent_invalid_type_period_update_for_refs() RETURNS TRIGGER AS $$
            BEGIN
                IF (NEW.effect_date_time <> OLD.effect_date_time OR NEW.expire_date_time <> OLD.expire_date_time) THEN
                    IF EXISTS (
                        SELECT 1 FROM type_attributes attr WHERE attr.workspace = NEW.workspace AND attr.type_id = NEW.id AND attr.type_version = NEW.version
                          AND attr.attribute_type ->> 'referenced_type' IS NOT NULL
                          AND (
                            SELECT COUNT(*) > 0 FROM (
                                 SELECT MIN(effect_date_time) AS combined_start, MAX(expire_date_time) AS combined_end,
                                     SUM(CASE WHEN previous_expire_date_time IS NOT NULL AND previous_expire_date_time <> effect_date_time THEN 1 ELSE 0 END) AS gaps
                                 FROM (
                                          SELECT effect_date_time, expire_date_time, LAG(expire_date_time) OVER (ORDER BY effect_date_time) AS previous_expire_date_time
                                          FROM types WHERE workspace = NEW.workspace AND id = attr.attribute_type ->> 'referenced_type'
                                            AND (effect_date_time, expire_date_time) OVERLAPS (NEW.effect_date_time, NEW.expire_date_time)
                                      ) sub
                            ) merged
                            WHERE gaps > 0 OR combined_start > NEW.effect_date_time OR combined_end < NEW.expire_date_time
                          )
                    ) THEN
                        RAISE EXCEPTION 'Cannot modify type period as its attribute references would have gaps or lack coverage.';
                    END IF;
                END IF;
                RETURN NEW;
            END;
            $$ LANGUAGE plpgsql;
            CREATE CONSTRAINT TRIGGER prevent_invalid_type_period_update_for_refs_trigger AFTER UPDATE ON types FOR EACH ROW EXECUTE FUNCTION prevent_invalid_type_period_update_for_refs();
        """).then().block()
    }

    afterSpec { postgres.stop() }

    Given("참조 무결성 위반 테스트") {
        val ws = UUID.randomUUID()
        val typeB = Type.create(
            "typeB", "1.0",
            Instant.parse("2026-05-25T00:00:00Z").toEpochMilli().toDouble(),
            253402214400000.0
        ).description("5/25일부터 유효한 타입").primitive(false)

        val typeA = Type.create(
            "typeA", "1.0",
            Instant.parse("2026-05-25T00:00:00Z").toEpochMilli().toDouble(),
            253402214400000.0
        ).description("5/25일부터 유효한 타입").primitive(false).attributes(
            arrayOf(
                Attribute.create(UUID.randomUUID().toString(), "ref", 0, AttributeType.document("typeB"))
            )
        )

        When("유효한 참조 상태에서 소유자 타입 A의 기간을 과거로 확장하면 (B가 존재하지 않는 기간까지)") {
            Then("데이터베이스 트리거에 의해 수정이 거부된다") {
                val saved = adapter.save(ws, listOf(typeB, typeA)).collectList().block()!!
                val savedA = saved.find { it.id() == "typeA" }!!
                val expandedA = savedA.withAttributes(savedA.attributes()).effectDateTime(0.0)
                
                StepVerifier.create(adapter.save(ws, listOf(expandedA)))
                    .expectErrorMatches { it.message!!.contains("Cannot modify type period as its attribute references would have gaps or lack coverage") }
                    .verify()
            }
        }

        When("전 기간 유효한 A가 일부 기간만 유효한 B를 참조하여 저장하면") {
            Then("데이터베이스 트리거에 의해 예외가 발생한다") {
                val typeA_invalid = Type.create(
                    "typeA_invalid", "1.0",
                    0.0,
                    253402214400000.0
                ).description("전 기간 유효한 타입").primitive(false).attributes(
                    arrayOf(
                        Attribute.create(UUID.randomUUID().toString(), "ref", 0, AttributeType.document("typeB"))
                    )
                )
                // typeB는 이미 저장됨 (위 When에서)
                StepVerifier.create(adapter.save(ws, listOf(typeA_invalid)))
                    .expectErrorMatches { it.message!!.contains("Referenced type (id=typeB) has gaps or does not fully cover the owner type period") }
                    .verify()
            }
        }
    }

    Given("타입 저장") {
        val type = Type.create(
            "customer",
            "1.0",
            Instant.parse("2026-01-01T00:00:00Z").toEpochMilli().toDouble(),
            Instant.parse("2026-12-31T23:59:59Z").toEpochMilli().toDouble(),
        ).description("고객 타입").primitive(false).attributes(
            arrayOf(
                Attribute.create(UUID.randomUUID().toString(), "name", 0, AttributeType.text())
                    .description("고객 이름")
                    .nullable(false)
                    .inherited(false),
                Attribute.create(UUID.randomUUID().toString(), "age", 1, AttributeType.number(0.0, 200.0))
                    .description("나이")
                    .nullable(true)
                    .inherited(false),
            )
        )

        When("save를 호출하면") {
            Then("저장된 타입이 반환된다") {
                StepVerifier.create(adapter.save(workspace, listOf(type)))
                    .assertNext { saved ->
                        saved.id() shouldBe "customer"
                        saved.version() shouldBe "1.0"
                        saved.description() shouldBe "고객 타입"
                        saved.primitive() shouldBe false
                        saved.rev() shouldBe 0.0
                    }
                    .verifyComplete()
            }
            Then("이미 존재하는 타입을 다시 save하면 UPDATE가 수행된다") {
                val start = Instant.EPOCH
                val end = Instant.ofEpochMilli(253402214400000L)
                val saved = adapter.findByWorkspaceAndPeriod(workspace, start, end).blockFirst()!!
                val updatedType = saved.withAttributes(saved.attributes()).description("수정된 설명")
                
                StepVerifier.create(adapter.save(workspace, listOf(updatedType)))
                    .assertNext { updated ->
                        updated.id() shouldBe "customer"
                        updated.description() shouldBe "수정된 설명"
                        updated.rev() shouldBe 1.0 // 0 -> 1 증가 확인
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
                        found.id() shouldBe "customer"
                        found.version() shouldBe "1.0"
                        found.attributes().size shouldBe 2
                        found.attributes()[0].name() shouldBe "name"
                        found.attributes()[1].name() shouldBe "age"
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
                        Attribute.create(UUID.randomUUID().toString(), "phone", 2, AttributeType.text())
                            .description("전화번호")
                            .nullable(true)
                            .inherited(false),
                    ),
                )
                StepVerifier.create(adapter.patch(workspace, listOf(patch)))
                    .assertNext { patched ->
                        patched.id() shouldBe "customer"
                        patched.attributes().size shouldBe 3
                        patched.attributes().any { it.name() == "name" } shouldBe true
                        patched.attributes().any { it.name() == "age" } shouldBe true
                        patched.attributes().any { it.name() == "phone" } shouldBe true
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
                        Attribute.create(UUID.randomUUID().toString(), "email", 3, AttributeType.text())
                            .description("이메일")
                            .nullable(true)
                            .inherited(false),
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
