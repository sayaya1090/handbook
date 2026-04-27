package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Group
import dev.sayaya.handbook.domain.User
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.util.*

@Service
class GroupSearchService(private val groupReadRepo: GroupReadRepository) {

    fun listGroups(workspaceId: UUID): Flux<Group> =
        groupReadRepo.findAllByWorkspace(workspaceId)

    fun listMembers(workspaceId: UUID, groupId: UUID): Flux<User> =
        groupReadRepo.findMembersByGroup(workspaceId, groupId)
}
