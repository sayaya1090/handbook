package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.usecase.RoleRepository
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Repository
class R2dbcRoleRepositoryAdapter(
    private val template: R2dbcEntityTemplate
) : RoleRepository {

    override fun findByGroup(workspaceId: UUID, groupId: UUID): Flux<String> {
        val criteria = Query.query(
            Criteria.where("workspace").`is`(workspaceId)
                .and("group_id").`is`(groupId)
        )
        return template.select(criteria, R2dbcGroupRoleEntity::class.java)
            .map { it.roleName }
    }

    override fun saveMapping(workspaceId: UUID, groupId: UUID, roleName: String): Mono<Void> {
        val entity = R2dbcGroupRoleEntity(
            workspace = workspaceId,
            groupId = groupId,
            roleName = roleName
        )
        return template.insert(entity).then()
    }

    override fun deleteMapping(workspaceId: UUID, groupId: UUID, roleName: String): Mono<Void> {
        val criteria = Query.query(
            Criteria.where("workspace").`is`(workspaceId)
                .and("group_id").`is`(groupId)
                .and("role_name").`is`(roleName)
        )
        return template.delete(criteria, R2dbcGroupRoleEntity::class.java).then()
    }
}
