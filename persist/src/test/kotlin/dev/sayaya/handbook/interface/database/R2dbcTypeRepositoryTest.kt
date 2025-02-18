package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.testcontainer.Database
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.every
import io.mockk.mockk
import org.slf4j.LoggerFactory
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.ReactiveAuditorAware
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.annotation.EnableTransactionManagement
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@DataR2dbcTest(properties = [
    "logging.level.io.r2dbc.postgresql.QUERY=DEBUG",
    "logging.level.io.r2dbc.postgresql.PARAM=DEBUG",
]) @Import(R2dbcTypeRepositoryTest.Companion.TestConfig::class)
class R2dbcTypeRepositoryTest(
    private val template: R2dbcEntityTemplate,
    private val databaseClient: DatabaseClient
) : ShouldSpec({
    val attributeRepo = mockk<R2dbcAttributeRepository>()
    val repository = R2dbcTypeRepository(template, attributeRepo)

    beforeSpec {
        databaseClient.sql("""
            INSERT INTO "user" (id, last_modified_at, created_at, last_login_at, name) 
            VALUES ('system', NOW(), NOW(), null, 'system')
        """.trimIndent()
        ).fetch().rowsUpdated().let(StepVerifier::create).expectNextCount(1).verifyComplete()

        every { attributeRepo.findByType(any()) } returns Flux.empty()
        every { attributeRepo.save(any(), any()) } answers { Mono.just(secondArg()) }
    }
    beforeTest {
        databaseClient.sql("DELETE FROM type").fetch().rowsUpdated().let(StepVerifier::create).expectNextCount(1).verifyComplete()
    }
    should("ID가 없는 경우 새로운 Type을 삽입한다") {
        // Given
        val type = Type(
            id = "type1",
            primitive = true,
            description = "New Type",
            parent = null,
            attributes = emptyList()
        )

        // When
        val result = repository.save(type)

        // Then
        StepVerifier.create(result).expectNextMatches {
            it.id == "type1" &&
            it.description == "New Type" &&
            it.primitive &&
            it.parent == null
        }.verifyComplete()
    }

    should("ID가 있는 경우 Type을 업데이트한다") {
        // Given
        databaseClient.sql("""
            INSERT INTO type (id, parent) VALUES ('parent1', null);
            INSERT INTO type (id, parent) VALUES ('type1', null);
        """).fetch().rowsUpdated().block()

        val updatedType = Type(
            id = "type1",
            primitive = false,
            description = "Updated Type",
            parent = "parent1",
            attributes = emptyList()
        )

        // When
        val result = repository.save(updatedType)

        // Then
        StepVerifier.create(result).expectNextMatches {
            it.id == "type1" &&
            it.description == "Updated Type" &&
            it.primitive.not() &&
            it.parent == "parent1"
        }.verifyComplete()
    }
}) {
    companion object {
        @TestConfiguration
        @EnableR2dbcRepositories
        @EnableTransactionManagement
        @EnableR2dbcAuditing
        class TestConfig {
            private val logger = LoggerFactory.getLogger(TestConfig::class.java)
            @Bean fun auditorProvider(): ReactiveAuditorAware<String> = ReactiveAuditorAware {
                Mono.just("system")
            }
        }
        @JvmStatic
        @DynamicPropertySource
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            Database().registerDynamicProperties(registry)
        }
    }
}