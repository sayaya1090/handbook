package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Group
import reactor.core.publisher.Mono
import java.util.*

/**
 * 그룹 및 멤버 관리 유스케이스.
 *
 * **책임:** 워크스페이스 내 그룹 CRUD 및 멤버 배정 비즈니스 로직 처리.
 *
 * **의존관계:** [GroupRepository] — 데이터 영속화 포트.
 */
class GroupService(private val groupRepo: GroupRepository) {

    fun createGroup(workspaceId: UUID, name: String, description: String?): Mono<Group> {
        val group = Group.create(UUID.randomUUID().toString(), workspaceId.toString(), name, description)
        return groupRepo.save(group)
    }

    fun deleteGroup(workspaceId: UUID, groupId: UUID): Mono<Void> =
        groupRepo.delete(workspaceId, groupId)

    fun addMember(workspaceId: UUID, groupId: UUID, userId: UUID): Mono<Void> =
        groupRepo.addMember(groupId, userId)

    fun removeMember(workspaceId: UUID, groupId: UUID, userId: UUID): Mono<Void> =
        groupRepo.removeMember(groupId, userId)
}
