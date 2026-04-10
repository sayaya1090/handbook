package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Group
import dev.sayaya.handbook.domain.Workspace
import dev.sayaya.handbook.usecase.GroupRepository
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.security.Principal
import java.util.*

@Repository
class R2dbcGroupRepositoryAdapter(
    private val template: R2dbcEntityTemplate,
) : GroupRepository {

    override fun createAndAssign(workspace: Workspace, creator: Principal, name: String, description: String?): Mono<Group> {
        val groupEntity = R2dbcGroupEntity(workspace = workspace.id, name = name)
        val memberEntity = R2dbcGroupMemberEntity(
            workspace = workspace.id,
            group = name,
            member = UUID.fromString(creator.name),
        )
        return template.insert(groupEntity)
            .delayUntil { template.insert(memberEntity) }
            .map { Group(UUID.randomUUID(), workspace.id, name, description) }
    }
}
