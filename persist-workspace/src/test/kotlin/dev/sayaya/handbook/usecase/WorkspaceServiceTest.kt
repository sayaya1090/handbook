package dev.sayaya.handbook.usecase

import io.kotest.core.spec.style.ShouldSpec
import io.mockk.*
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@Suppress("ReactiveStreamsUnusedPublisher")
internal class WorkspaceServiceTest: ShouldSpec({
    val repo = mockk<WorkspaceRepository>()
    val eventHandler = mockk<ExternalServiceHandler>()
    val service = WorkspaceService(repo, eventHandler)

    beforeTest {
        clearMocks(repo, eventHandler)
    }
    // Given
    val workspaceBuilder = spyk(WorkspaceBuilder(name="test"))
    val workspace = workspaceBuilder.build()
    every { workspaceBuilder.build() } returns workspace

    should("워크스페이스 저장에 성공하면 이벤트핸들러에 저장된 타입을 전달") {
        every { repo.save(workspace) } returns Mono.just(workspace)
        every { eventHandler.publish(workspace) } returns Mono.empty()

        // When & Then
        service.save(workspaceBuilder).let(StepVerifier::create).expectNext(workspace).verifyComplete()
        verify(exactly = 1) { repo.save(workspace) }
        verify(exactly = 1) { eventHandler.publish(workspace) }
    }

    should("워크스페이스 저장에 실패하면 이벤트핸들러에 저장된 워크스페이스를 전달하지 않음") {
        every { repo.save(workspace) } returns Mono.error(RuntimeException("DB 에러"))
        every { eventHandler.publish(workspace) } returns Mono.empty()

        // When & Then
        service.save(workspaceBuilder).let(StepVerifier::create).verifyError(RuntimeException::class.java)
        verify(exactly = 1) { repo.save(workspace) }
        verify(exactly = 0) { eventHandler.publish(workspace) }
    }
})