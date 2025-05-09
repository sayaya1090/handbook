package dev.sayaya.handbook.usecase.type

import dev.sayaya.handbook.domain.Layout
import dev.sayaya.handbook.domain.Type
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.security.Principal
import java.time.Instant
import java.util.*

@Suppress("ReactiveStreamsUnusedPublisher")
internal class TypeServiceTest: ShouldSpec({
    val repo = mockk<TypeRepository>()

    val eventHandler = mockk<ExternalServiceHandler>()
    val principal = mockk<Principal>()
    val service = TypeService(repo, eventHandler)

    beforeTest {
        clearMocks(repo, eventHandler, principal)
    }
    // Given
    val workspace = UUID.fromString("398f6038-2192-417b-914a-f74e4bf52451")
    val testType = Type(
        id = "test",
        version = "1.0",
        parent = null,
        effectDateTime = Instant.parse("2024-01-01T00:00:00Z"),
        expireDateTime = Instant.parse("2024-12-31T23:59:59Z"),
        description = "test type",
        primitive = false,
        attributes = emptyList(),
        x = 10u,
        y = 20u,
        width = 100u,
        height = 50u
    )
/*
    should("타입 저장 과정 전체가 성공적으로 완료됨") {
        // Given - 모든 단계 성공 시나리오
        every { repo.saveAll(workspace, any<List<Type>>()) } returns Mono.just(listOf(testType))
        every { eventHandler.publish(principal, workspace, typeWithLayouts) } returns Mono.empty()

        // When & Then
        service.save(principal, workspace, typeWithLayouts).let(StepVerifier::create).expectNext(typeWithLayouts).verifyComplete()
        verify(exactly = 1) { repo.saveAll(workspace, listOf(testType)) }
        verify(exactly = 1) { eventHandler.publish(principal, workspace, typeWithLayouts) }
    }
    should("타입 저장 실패 시 레이아웃 생성을 시도하지 않음") {
        // Given
        val error = RuntimeException("DB 오류")
        every { repo.saveAll(workspace, any<List<Type>>()) } returns Mono.error(error)

        // When & Then
        service.save(principal, workspace, typeWithLayouts)
            .let(StepVerifier::create)
            .verifyErrorSatisfies { e ->
                e is RuntimeException && e.message == "DB 오류"
            }

        // 검증
        verify(exactly = 1) { repo.saveAll(workspace, listOf(testType)) }
        verify(exactly = 0) { eventHandler.publish(any(), any(), any()) }
    }

    should("레이아웃 생성 실패 시 레이아웃 저장을 시도하지 않음") {
        // Given
        val error = RuntimeException("레이아웃 생성 오류")
        every { repo.saveAll(workspace, any<List<Type>>()) } returns Mono.just(listOf(testType))

        // When & Then
        service.save(principal, workspace, typeWithLayouts)
            .let(StepVerifier::create)
            .verifyErrorSatisfies { e ->
                e is RuntimeException && e.message == "레이아웃 생성 오류"
            }

        // 검증
        verify(exactly = 1) { repo.saveAll(workspace, listOf(testType)) }
        verify(exactly = 0) { eventHandler.publish(any(), any(), any()) }
    }

    should("레이아웃 저장 실패 시 이벤트 발행을 시도하지 않음") {
        // Given
        val error = RuntimeException("레이아웃 저장 오류")
        every { repo.saveAll(workspace, any<List<Type>>()) } returns Mono.just(listOf(testType))

        // When & Then
        service.save(principal, workspace, typeWithLayouts)
            .let(StepVerifier::create)
            .verifyErrorSatisfies { e ->
                e is RuntimeException && e.message == "레이아웃 저장 오류"
            }

        // 검증
        verify(exactly = 1) { repo.saveAll(workspace, listOf(testType)) }
        verify(exactly = 0) { eventHandler.publish(any(), any(), any()) }
    }

    should("이벤트 발행 실패 시에도 타입 및 레이아웃은 저장되고 완료됨") {
        // Given - 이벤트 발행만 실패하는 시나리오
        val error = RuntimeException("이벤트 발행 오류")
        every { repo.saveAll(workspace, any<List<Type>>()) } returns Mono.just(listOf(testType))
        every { eventHandler.publish(principal, workspace, typeWithLayouts) } returns Mono.error(error)

        // When & Then
        service.save(principal, workspace, typeWithLayouts)
            .let(StepVerifier::create)
            .verifyErrorSatisfies { e ->
                e is RuntimeException && e.message == "이벤트 발행 오류"
            }

        // 검증 - 모든 단계가 호출됨
        verify(exactly = 1) { repo.saveAll(workspace, listOf(testType)) }
        verify(exactly = 1) { eventHandler.publish(principal, workspace, typeWithLayouts) }
    }

    should("복수의 타입을 포함한 레이아웃이 성공적으로 처리됨") {
        // Given
        val secondType = Type(
            id = "test2",
            version = "1.0",
            parent = null,
            effectDateTime = Instant.parse("2024-01-10T00:00:00Z"),
            expireDateTime = Instant.parse("2024-12-20T23:59:59Z"),
            description = "another test type",
            primitive = false,
            attributes = emptyList(),
            x = 200u,
            y = 100u,
            width = 150u,
            height = 75u
        )

        val allTypes = listOf(testType, secondType)

        every { repo.saveAll(workspace, allTypes) } returns Mono.just(allTypes)
        every { eventHandler.publish(principal, workspace, multipleTypeWithLayouts) } returns Mono.empty()

        // When & Then
        service.save(principal, workspace, multipleTypeWithLayouts)
            .let(StepVerifier::create)
            .expectNext(multipleTypeWithLayouts)
            .verifyComplete()

        // 검증
        verify(exactly = 1) { repo.saveAll(workspace, allTypes) }
        verify(exactly = 1) { eventHandler.publish(principal, workspace, multipleTypeWithLayouts) }
    }*/

    should("빈 타입 목록으로 호출 시에는 아무것도 저장하지 않음") {
        // Given
        val emptyTypeWithLayouts = emptyList<Type>()

        // When & Then
        service.save(principal, workspace, emptyTypeWithLayouts)
            .let(StepVerifier::create)
            .verifyComplete()

        // 검증
        verify(exactly = 0) { repo.saveAll(any(), any()) }
        verify(exactly = 0) { eventHandler.publish(any(), any(), any()) }
    }
})