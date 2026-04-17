package dev.sayaya.handbook.interfaces.database

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.ConnectionFactoryOptions.DATABASE
import io.r2dbc.spi.ConnectionFactoryOptions.DRIVER
import io.r2dbc.spi.ConnectionFactoryOptions.HOST
import io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD
import io.r2dbc.spi.ConnectionFactoryOptions.PORT
import io.r2dbc.spi.ConnectionFactoryOptions.USER
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.DatabaseClient
import org.testcontainers.postgresql.PostgreSQLContainer
import reactor.test.StepVerifier
import java.util.UUID

class R2dbcWorkspaceReadAdapterIntegrationTest : BehaviorSpec({
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
    val adapter = R2dbcWorkspaceReadAdapter(template)
    val client = DatabaseClient.create(connectionFactory)

    val wsId1 = UUID.randomUUID()
    val wsId2 = UUID.randomUUID()

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
            INSERT INTO workspace (id, name, description) VALUES
            ('$wsId1', 'alpha', 'first workspace'),
            ('$wsId2', 'beta', null)
        """).then().block()
    }

    afterSpec { postgres.stop() }

    Given("workspace 테이블에 두 건이 적재된 상태에서") {
        When("findAll 을 호출하면") {
            Then("모든 워크스페이스가 도메인 객체로 매핑되어 반환된다") {
                StepVerifier.create(adapter.findAll().collectList())
                    .assertNext { list ->
                        list.map { it.id } shouldContainExactlyInAnyOrder listOf(wsId1, wsId2)
                        list.first { it.id == wsId1 }.name shouldBe "alpha"
                        list.first { it.id == wsId1 }.description shouldBe "first workspace"
                        list.first { it.id == wsId2 }.description shouldBe null
                    }
                    .verifyComplete()
            }
        }

        When("findById 로 단건을 조회하면") {
            Then("일치하는 워크스페이스가 반환된다") {
                StepVerifier.create(adapter.findById(wsId1))
                    .assertNext { ws ->
                        ws.id shouldBe wsId1
                        ws.name shouldBe "alpha"
                    }
                    .verifyComplete()
            }
            Then("존재하지 않는 id 로 조회하면 빈 결과") {
                StepVerifier.create(adapter.findById(UUID.randomUUID()))
                    .verifyComplete()
            }
        }
    }
})
