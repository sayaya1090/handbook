package dev.sayaya.handbook.usecase

import io.kotest.core.spec.style.ShouldSpec
import io.mockk.*
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.security.Principal

@Suppress("ReactiveStreamsUnusedPublisher")
internal class WorkspaceServiceTest: ShouldSpec({
    val workspaceRepo = mockk<WorkspaceRepository>()
    val groupRepo = mockk<GroupRepository>()
    val eventHandler = mockk<ExternalServiceHandler>()
    val principal = mockk<Principal>()
    val service = WorkspaceService(workspaceRepo, groupRepo, eventHandler)

    beforeTest {
        clearMocks(workspaceRepo)
    }
    // Given
    val workspaceBuilder = spyk(WorkspaceBuilder(name="test"))
    val workspace = workspaceBuilder.build()
    every { workspaceBuilder.build() } returns workspace
    every { groupRepo.createAndAssign(workspace, principal, any(), any()) } returns Mono.just(mockk())
    every { eventHandler.publish(workspace) } returns Mono.empty()

    should("워크스페이스 저장에 성공하면 이벤트핸들러에 저장된 타입을 전달") {
        every { workspaceRepo.save(workspace) } returns Mono.just(workspace)

        // When & Then
        service.save(principal, workspaceBuilder).let(StepVerifier::create).expectNext(workspace).verifyComplete()
        verify(exactly = 1) { workspaceRepo.save(workspace) }
        verify(exactly = 1) { groupRepo.createAndAssign(workspace, principal, any(), any()) }
        verify(exactly = 1) { eventHandler.publish(workspace) }
    }

    should("워크스페이스 저장에 실패하면 이벤트핸들러에 저장된 워크스페이스를 전달하지 않음") {
        every { workspaceRepo.save(workspace) } returns Mono.error(RuntimeException("DB 에러"))

        // When & Then
        service.save(principal, workspaceBuilder).let(StepVerifier::create).verifyError(RuntimeException::class.java)
        verify(exactly = 1) { workspaceRepo.save(workspace) }
        verify(exactly = 0) { groupRepo.createAndAssign(workspace, principal, any(), any()) }
        verify(exactly = 0) { eventHandler.publish(workspace) }
    }
})