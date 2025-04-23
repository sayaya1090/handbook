package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Workspace
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import java.security.Principal

@Service
class WorkspaceService(
    private val workspaceRepo: WorkspaceRepository,
    private val groupRepo: GroupRepository,
    private val eventHandler: ExternalServiceHandler,
) {
    // 새로운 워크스페이스 생성 시, 워크스페이스를 생성하고 Workspace Admin 그룹을 만들어 생성자를 배정한다.
    @Transactional
    fun save(principal: Principal, workspaceBuilder: WorkspaceBuilder): Mono<Workspace> = workspaceBuilder.build()
        .let(workspaceRepo::save)
        .delayUntil { groupRepo.createAndAssign(
            workspace = it,
            creator = principal,
            name = GROUP_ADMIN,
            description = null,
        ) }.delayUntil(eventHandler::publish)

    companion object {
        const val GROUP_ADMIN = "Admin"
    }
}