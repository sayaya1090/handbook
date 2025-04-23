package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Workspace
import dev.sayaya.handbook.testcontainer.Database
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.ReactiveAuditorAware
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.*

@DataR2dbcTest(properties = [
    "logging.level.io.r2dbc.postgresql.QUERY=DEBUG",
    "logging.level.io.r2dbc.postgresql.PARAM=DEBUG",
]) @Import(R2dbcWorkspaceRepositoryTest.Companion.TestConfig::class)
class R2dbcWorkspaceRepositoryTest(
    private val template: R2dbcEntityTemplate,
    private val databaseClient: DatabaseClient
) : ShouldSpec({
    val repository = R2dbcWorkspaceRepository(template)
    val workspace = Workspace(
        id = UUID.fromString("398f6038-2192-417b-914a-f74e4bf52451"),
        name = "Test Workspace",
        description = "Test Description"
    )
    beforeSpec {
        databaseClient.sql("""
            INSERT INTO public."user" (id, last_modified_at, created_at, last_login_at, name, provider, account) 
            VALUES ('93951bc3-be1e-4fc8-865f-d6376ac3e87b', NOW(), NOW(), null, 'system', 'handbook', 'system');
        """.trimIndent()
        ).fetch().rowsUpdated().let(StepVerifier::create).expectNextCount(1).verifyComplete()
    }
    beforeTest {
        databaseClient.sql("DELETE FROM workspace").fetch().rowsUpdated().let(StepVerifier::create).expectNextCount(1).verifyComplete()
    }
    should("저장된 워크스페이스를 반환해야 한다") {
        repository.save(workspace).let(StepVerifier::create).assertNext {
            it.id shouldBe workspace.id
            it.name shouldBe workspace.name
            it.description shouldBe workspace.description
        }.verifyComplete()
    }

    should("중복된 워크스페이스를 삽입 시 예외가 발생해야 한다") {
        // Given
        val duplicated = Workspace(
            id = workspace.id,
            name = "Duplicate Workspace",
            description = "Duplicate Description"
        )
        repository.save(workspace).let(StepVerifier::create).expectNextCount(1).verifyComplete()

        // When
        repository.save(duplicated).let(StepVerifier::create).expectError().verify()
    }

}) {
    companion object {
        @TestConfiguration
        @EnableR2dbcAuditing
        class TestConfig {
            private val logger = LoggerFactory.getLogger(TestConfig::class.java)
            @Bean
            fun auditorProvider(): ReactiveAuditorAware<UUID> = ReactiveAuditorAware {
                Mono.just(UUID.fromString("93951bc3-be1e-4fc8-865f-d6376ac3e87b"))
            }
        }
        @JvmStatic
        @DynamicPropertySource
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            Database().registerDynamicProperties(registry)
        }
    }
}