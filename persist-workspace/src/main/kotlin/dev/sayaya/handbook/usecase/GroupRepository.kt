package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Group
import dev.sayaya.handbook.domain.Workspace
import reactor.core.publisher.Mono
import java.security.Principal
import java.util.*

/**
 * 워크스페이스 그룹 관리 저장소 포트.
 *
 * **책임:** 그룹 생성 및 멤버 배정, 기존 그룹에 멤버 추가를 담당한다.
 *
 * **의존관계:** [R2dbcGroupRepositoryAdapter][dev.sayaya.handbook.interfaces.database.R2dbcGroupRepositoryAdapter]가 구현한다.
 */
interface GroupRepository {
    fun createAndAssign(workspace: Workspace, creator: Principal, name: String, description: String?): Mono<Group>

    /**
     * 기존 워크스페이스의 기본 그룹(Member)에 사용자를 추가한다.
     *
     * @param workspaceId 워크스페이스 ID
     * @param principal 참여 요청자
     * @return 완료 시그널
     */
    fun addMember(workspaceId: UUID, principal: Principal): Mono<Void>

    /**
     * 주어진 워크스페이스에 속한 모든 그룹과 그룹 멤버 row 를 삭제한다.
     * 워크스페이스 cascade 삭제의 일부로 호출된다.
     *
     * @param workspaceId 워크스페이스 ID
     * @return 완료 시그널
     */
    fun deleteByWorkspace(workspaceId: UUID): Mono<Void>
}
