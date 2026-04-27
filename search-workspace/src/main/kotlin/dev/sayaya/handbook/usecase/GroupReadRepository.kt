package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Group
import dev.sayaya.handbook.domain.User
import reactor.core.publisher.Flux
import java.util.*

interface GroupReadRepository {
    fun findAllByWorkspace(workspaceId: UUID): Flux<Group>
    fun findMembersByGroup(workspaceId: UUID, groupId: UUID): Flux<User>
}
