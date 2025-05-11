package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.testcontainer.Database
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.slf4j.LoggerFactory
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.ReactiveAuditorAware
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant
import java.util.*

@DataR2dbcTest(properties = [
    "logging.level.io.r2dbc.postgresql.QUERY=DEBUG",
    "logging.level.io.r2dbc.postgresql.PARAM=DEBUG",
]) @Import(R2dbcTypeRepositoryTest.Companion.TestConfig::class)
class R2dbcTypeRepositoryTest(
    private val databaseClient: DatabaseClient
) : ShouldSpec({
    /*val childRepo = mockk<R2dbcAttributeRepository>().apply {
        every { save(any(), any()) } answers { Mono.just(secondArg()) }
    }
    val repository = R2DbcTypeRepository(databaseClient, childRepo)
    val workspace = UUID.fromString("398f6038-2192-417b-914a-f74e4bf52451")
    beforeSpec {
        databaseClient.sql("""
            INSERT INTO public."user" (id, last_modified_at, created_at, last_login_at, name, provider, account) 
            VALUES ('93951bc3-be1e-4fc8-865f-d6376ac3e87b', NOW(), NOW(), null, 'system', 'handbook', 'system');
        """.trimIndent()
        ).fetch().rowsUpdated().let(StepVerifier::create).expectNextCount(1).verifyComplete()
    }
    beforeTest {
        databaseClient.sql("DELETE FROM type").fetch().rowsUpdated().let(StepVerifier::create).expectNextCount(1).verifyComplete()
        clearMocks(childRepo,
            recordedCalls = true,    // 호출 기록 초기화
            answers = false          // 스텁된 응답 유지
        )
    }
    should("부모 타입의 유효기간을 자식과의 겹치는 구간보다 짧게 업데이트하면 실패한다") {
        // Given: 부모와 자식 타입 생성
        val parentType = Type(
            id = "update_parent",
            version = "1.0",
            parent = null,
            effectDateTime = Instant.parse("2025-01-01T00:00:00Z"),
            expireDateTime = Instant.parse("2025-12-31T23:59:59Z"),
            description = "", primitive = false, attributes = emptyList()
        )
        val childType = Type(
            id = "update_child",
            version = "1.0",
            parent = "update_parent",
            effectDateTime = Instant.parse("2025-03-01T00:00:00Z"),
            expireDateTime = Instant.parse("2025-08-31T23:59:59Z"),
            description = "", primitive = false, attributes = emptyList()
        )

        // When: 부모와 자식 데이터 저장
        repository.save(workspace, parentType)
            .then(repository.save(workspace, childType))
            .let(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        clearMocks(childRepo, recordedCalls = true, answers = false)
        // Then: 부모의 유효기간을 자식과의 겹치는 구간보다 짧게 업데이트 시도
        val updatedParentType = parentType.copy(
            effectDateTime = Instant.parse("2025-03-01T00:00:00Z"),
            expireDateTime = Instant.parse("2025-07-31T23:59:59Z")
        )
        repository.save(workspace, updatedParentType)
            .let(StepVerifier::create)
            .verifyError(RuntimeException::class.java)

        verify(exactly = 0) { childRepo.save(any(), any()) }
    }
    should("부모 타입의 유효기간을 자식과의 겹치는 구간을 포함하도록 업데이트하면 성공한다") {
        // Given: 부모와 자식 타입 생성
        val parentType = Type(
            id = "update_parent",
            version = "1.0",
            parent = null,
            effectDateTime = Instant.parse("2025-01-01T00:00:00Z"),
            expireDateTime = Instant.parse("2025-12-31T23:59:59Z"),
            description = "", primitive = false, attributes = emptyList()
        )
        val childType = Type(
            id = "update_child",
            version = "1.0",
            parent = "update_parent",
            effectDateTime = Instant.parse("2025-03-01T00:00:00Z"),
            expireDateTime = Instant.parse("2025-08-31T23:59:59Z"),
            description = "", primitive = false, attributes = emptyList()
        )
        // When: 부모와 자식 데이터 저장
        repository.save(workspace, parentType)
            .then(repository.save(workspace, childType))
            .let(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        // Then: 부모의 유효기간을 자식과의 겹치는 구간을 포함하도록 업데이트
        val updatedParentType = parentType.copy(
            effectDateTime = Instant.parse("2025-02-01T00:00:00Z"),
            expireDateTime = Instant.parse("2025-09-30T23:59:59Z")
        )
        clearMocks(childRepo, recordedCalls = true, answers = false)
        repository.save(workspace, updatedParentType)
            .let(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        verify(exactly = 1) { childRepo.save(any(), any()) }
    }*/
}) {
    companion object {
        @TestConfiguration
        @EnableR2dbcAuditing
        class TestConfig {
            private val logger = LoggerFactory.getLogger(TestConfig::class.java)
            @Bean fun auditorProvider(): ReactiveAuditorAware<UUID> = ReactiveAuditorAware {
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