package dev.sayaya.handbook.usecase

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * 역할(Role) 관리 저장소 포트.
 *
 * **책임:** 그룹과 역할 간의 매핑 영속화 담당.
 */
interface RoleRepository {
    fun findByGroup(workspaceId: UUID, groupId: UUID): Flux<String>
    fun saveMapping(workspaceId: UUID, groupId: UUID, roleName: String): Mono<Void>
    fun deleteMapping(workspaceId: UUID, groupId: UUID, roleName: String): Mono<Void>
}
