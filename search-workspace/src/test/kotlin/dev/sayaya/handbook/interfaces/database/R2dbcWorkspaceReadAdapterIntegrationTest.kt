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
    val client = DatabaseClient.create(connectionFactory)
    val adapter = R2dbcWorkspaceReadAdapter(template, client)

    val wsId1 = UUID.randomUUID()
    val wsId2 = UUID.randomUUID()
    val wsId3 = UUID.randomUUID() // alice 가 속하지 않은 워크스페이스
    val alice = UUID.randomUUID()
    val bob = UUID.randomUUID()

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
            CREATE TABLE group_member (
                id UUID NOT NULL,
                workspace UUID NOT NULL,
                "group" UUID NOT NULL,
                member UUID NOT NULL,
                created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                PRIMARY KEY (id)
            )
        """).then().block()

        client.sql("""
            INSERT INTO workspace (id, name, description) VALUES
            ('$wsId1', 'alpha', 'first workspace'),
            ('$wsId2', 'beta', null),
            ('$wsId3', 'gamma', 'bob only')
        """).then().block()

        // alice 는 alpha(admin) + beta(Member) 소속, bob 은 gamma(admin) 소속.
        // 한 워크스페이스에서 다중 그룹 소속이어도 DISTINCT 로 한 번만 반환되는지 검증하기 위해
        // alice 를 alpha 의 admin 과 Member 두 그룹 모두에 등록한다.
        val gid1 = UUID.randomUUID()
        val gid2 = UUID.randomUUID()
        val gid3 = UUID.randomUUID()
        val gid4 = UUID.randomUUID()
        client.sql("""
            INSERT INTO group_member (id, workspace, "group", member) VALUES
            ('${UUID.randomUUID()}', '$wsId1', '$gid1', '$alice'),
            ('${UUID.randomUUID()}', '$wsId1', '$gid2', '$alice'),
            ('${UUID.randomUUID()}', '$wsId2', '$gid3', '$alice'),
            ('${UUID.randomUUID()}', '$wsId3', '$gid4', '$bob')
        """).then().block()
    }

    afterSpec { postgres.stop() }

    Given("workspace 테이블에 세 건이 적재되고 그룹 멤버십이 설정된 상태에서") {
        When("findAll 을 호출하면") {
            Then("모든 워크스페이스가 도메인 객체로 매핑되어 반환된다") {
                StepVerifier.create(adapter.findAll().collectList())
                    .assertNext { list ->
                        list.map { it.id } shouldContainExactlyInAnyOrder listOf(wsId1, wsId2, wsId3)
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

        When("findByUserSub(alice) 를 호출하면") {
            Then("alice 가 속한 alpha · beta 만 DISTINCT 로 반환된다") {
                StepVerifier.create(adapter.findByUserSub(alice).collectList())
                    .assertNext { list ->
                        list.map { it.id } shouldContainExactlyInAnyOrder listOf(wsId1, wsId2)
                    }
                    .verifyComplete()
            }
        }

        When("findByUserSub(bob) 를 호출하면") {
            Then("bob 이 속한 gamma 만 반환된다") {
                StepVerifier.create(adapter.findByUserSub(bob).collectList())
                    .assertNext { list ->
                        list.map { it.id } shouldContainExactlyInAnyOrder listOf(wsId3)
                    }
                    .verifyComplete()
            }
        }

        When("아무 그룹에도 속하지 않은 사용자로 findByUserSub 를 호출하면") {
            Then("빈 Flux 가 반환된다") {
                StepVerifier.create(adapter.findByUserSub(UUID.randomUUID()).collectList())
                    .assertNext { list -> list shouldBe emptyList() }
                    .verifyComplete()
            }
        }
    }
})
