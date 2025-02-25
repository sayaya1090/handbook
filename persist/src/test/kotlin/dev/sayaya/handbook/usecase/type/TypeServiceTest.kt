package dev.sayaya.handbook.usecase.type

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

class TypeServiceTest: ShouldSpec({
    val repo = mockk<TypeRepository>()
    val eventHandler = mockk<ExternalServiceHandler>()
    val service = TypeService(repo, eventHandler)

    beforeTest {
        clearMocks(repo, eventHandler)
    }
    // Given
    val principal = mockk<Principal>()
    val type = Type(
        id = "test",
        version = "1.0",
        parent = null,
        effectDateTime = Instant.parse("2024-01-01T00:00:00Z"),
        expireDateTime = Instant.parse("2024-12-31T23:59:59Z"),
        description = "test type",
        primitive = false,
        attributes = emptyList()
    )

    should("타입 저장에 성공하면 이벤트핸들러에 저장된 타입을 전달") {
        every { repo.save(type) } returns Mono.just(type)
        every { eventHandler.publish(type, principal) } returns Mono.empty()

        // When & Then
        service.save(type, principal).let(StepVerifier::create).verifyComplete()
        verify(exactly = 1) { repo.save(type) }
        verify(exactly = 1) { eventHandler.publish(type, principal) }
    }

    should("타입 저장에 실패하면 이벤트핸들러에 저장된 타입을 전달하지 않음") {
        every { repo.save(type) } returns Mono.error(RuntimeException("DB 에러"))
        every { eventHandler.publish(type, principal) } returns Mono.empty()

        // When & Then
        service.save(type, principal).let(StepVerifier::create).verifyError(RuntimeException::class.java)
        verify(exactly = 1) { repo.save(type) }
        verify(exactly = 0) { eventHandler.publish(type, principal) }
    }
})