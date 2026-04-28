package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Group
import dev.sayaya.handbook.domain.Workspace
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono
import java.security.Principal
import java.util.*

/**
 * 워크스페이스 CUD 및 참여 유스케이스.
 *
 * **책임:** 워크스페이스 생성/수정/삭제와 참여(join) 요청을 처리한다.
 * 삭제는 연관 그룹·그룹 멤버·웹훅 row 를 함께 제거하는 cascade 삭제를 단일 트랜잭션으로 수행한다.
 *
 * **의존관계:**
 * - [WorkspaceRepository] — 워크스페이스 엔티티 영속화
 * - [GroupRepository] — 그룹 생성/멤버 배정/워크스페이스 단위 삭제
 * - [WebhookService] — 웹훅 워크스페이스 단위 삭제
 * - [WorkspaceEventPublisher] — Kafka 이벤트 발행
 * - [TransactionalOperator] — cascade 삭제의 원자성 보장 (R2DBC reactive 트랜잭션)
 *
 * **주의:** create 시 Admin 그룹을 자동 생성하고, join 시 Member 그룹에 배정한다.
 * delete 의 cascade 범위는 현재 workspace-command 내부 테이블 (`group`, `group_member`, `webhooks`) 까지이며,
 * document/type 테이블 cascade 는 후속 반복에서 확장된다.
 */
class WorkspaceService(
    private val workspaceRepo: WorkspaceRepository,
    private val groupRepo: GroupRepository,
    private val webhookService: WebhookService,
    private val eventPublisher: WorkspaceEventPublisher,
    private val tx: TransactionalOperator,
) {
    /**
     * 새 워크스페이스를 생성한다. Admin 그룹을 함께 생성하고 생성자를 그 그룹의 첫 멤버로 배정한다.
     * 추가로 빈 Member 그룹을 자동 생성하여 join 요청 시 사용할 수 있게 한다.
     *
     * @param creator 생성자 사용자 UUID — `workspace.created_by` 및 `last_modified_by` 에 기록된다.
     *   `group_member.member` 와 동일 출처(Principal) 에서 추출되어야 감사 일관성이 유지된다.
     * @param principal GroupRepo 의 `createAndAssign` 에 전달되어 Admin 멤버로 배정된다.
     */
    fun create(creator: UUID, principal: Principal, name: String, description: String?): Mono<Workspace> {
        val workspace = Workspace(UUID.randomUUID(), name, description)
        return workspaceRepo.save(workspace, creator)
            .delayUntil { groupRepo.createAndAssign(it, principal, GROUP_ADMIN, null) }
            .delayUntil { groupRepo.save(Group(UUID.randomUUID(), workspace.id, GROUP_MEMBER, null)) }
            .delayUntil { eventPublisher.publishCreated(it) }
    }

    /**
     * 기존 워크스페이스를 수정한다.
     *
     * @param modifier 수정자 사용자 UUID — `workspace.last_modified_by` 에 기록된다.
     */
    fun update(workspace: Workspace, modifier: UUID): Mono<Workspace> =
        workspaceRepo.update(workspace, modifier)

    /**
     * 워크스페이스 삭제. 트랜잭션 내에서 참조하는 row(웹훅 → 그룹 멤버 → 그룹 → 워크스페이스)
     * 를 역순으로 삭제하고, 커밋 후 `WORKSPACE_DELETED` 이벤트를 발행한다.
     *
     * 이벤트 발행은 의도적으로 트랜잭션 밖이다 — DB 커밋 실패 시 Kafka 에 유령 이벤트가
     * 남지 않도록.
     */
    fun delete(id: UUID): Mono<Void> =
        webhookService.deleteByWorkspace(id)
            .then(groupRepo.deleteByWorkspace(id))
            .then(workspaceRepo.delete(id))
            .`as`(tx::transactional)
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
        const val GROUP_MEMBER = "Member"
    }
}
