package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Workspace
import reactor.core.publisher.Mono
import java.security.Principal
import java.util.*

/**
 * 워크스페이스 CUD 및 참여 유스케이스.
 *
 * **책임:** 워크스페이스 생성/수정/삭제와 참여(join) 요청을 처리한다.
 *
 * **의존관계:**
 * - [WorkspaceRepository] — 워크스페이스 엔티티 영속화
 * - [GroupRepository] — 그룹 생성/멤버 배정
 * - [WorkspaceEventPublisher] — Kafka 이벤트 발행
 *
 * **주의:** create 시 Admin 그룹을 자동 생성하고, join 시 Member 그룹에 배정한다.
 */
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

    /**
     * 기존 워크스페이스에 사용자를 Member 그룹으로 참여시킨다.
     *
     * @param workspaceId 참여할 워크스페이스 ID
     * @param principal 참여 요청자
     * @return 완료 시그널
     */
    fun join(workspaceId: UUID, principal: Principal?): Mono<Void> =
        if (principal != null) groupRepo.addMember(workspaceId, principal)
        else Mono.empty()

    companion object {
        const val GROUP_ADMIN = "Admin"
    }
}
