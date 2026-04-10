package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Workspace
import reactor.core.publisher.Mono
import java.security.Principal
import java.util.*

class WorkspaceService(
    private val workspaceRepo: WorkspaceRepository,
    private val groupRepo: GroupRepository,
    private val eventPublisher: WorkspaceEventPublisher,
) {
    fun create(principal: Principal, name: String, description: String?): Mono<Workspace> {
        val workspace = Workspace(UUID.randomUUID(), name, description)
        return workspaceRepo.save(workspace)
            .delayUntil { groupRepo.createAndAssign(it, principal, GROUP_ADMIN, null) }
            .delayUntil { eventPublisher.publishCreated(it) }
    }

    fun update(workspace: Workspace): Mono<Workspace> =
        workspaceRepo.update(workspace)

    fun delete(id: UUID): Mono<Void> =
        workspaceRepo.delete(id)
            .then(eventPublisher.publishDeleted(id))

    companion object {
        const val GROUP_ADMIN = "Admin"
    }
}
