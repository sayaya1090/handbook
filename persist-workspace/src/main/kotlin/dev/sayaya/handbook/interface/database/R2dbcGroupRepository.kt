package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Group
import dev.sayaya.handbook.domain.Workspace
import dev.sayaya.handbook.usecase.GroupRepository
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.security.Principal
import java.util.*

@Repository
class R2dbcGroupRepository(private val template: R2dbcEntityTemplate): GroupRepository {
    override fun createAndAssign(workspace: Workspace, creator: Principal, name: String, description: String?): Mono<Group> {
        val entity = R2dbcGroupEntity.of(workspace.id, name, description ?: "")
        val entity2 = R2dbcGroupMemberEntity(workspace.id, name, UUID.fromString(creator.name))
        return template.insert(entity).delayUntil { template.insert(entity2) }.map(this::map)
    }
    private fun map(entity: R2dbcGroupEntity): Group = Group(
        id=UUID.randomUUID(),
        workspace = entity.workspace,
        name = entity.name,
        description = ""
    )
}