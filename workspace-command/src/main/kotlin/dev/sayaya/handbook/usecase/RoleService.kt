package dev.sayaya.handbook.usecase

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * 역할(Role) 관리 유스케이스.
 *
 * **책임:** 그룹과 역할 간의 매핑 CRUD 처리.
 *
 * **의존관계:** [RoleRepository] — 데이터 영속화 포트.
 */
class RoleService(private val roleRepo: RoleRepository) {

    fun getRoles(workspaceId: UUID, groupId: UUID): Flux<String> =
        roleRepo.findByGroup(workspaceId, groupId)

    fun assignRole(workspaceId: UUID, groupId: UUID, roleName: String): Mono<Void> =
        roleRepo.saveMapping(workspaceId, groupId, roleName)

    fun removeRole(workspaceId: UUID, groupId: UUID, roleName: String): Mono<Void> =
        roleRepo.deleteMapping(workspaceId, groupId, roleName)
}
