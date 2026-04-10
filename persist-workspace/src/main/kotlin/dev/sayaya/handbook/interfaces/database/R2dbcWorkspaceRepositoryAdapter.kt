package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Workspace
import dev.sayaya.handbook.usecase.WorkspaceRepository
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.*

@Repository
class R2dbcWorkspaceRepositoryAdapter(
    private val template: R2dbcEntityTemplate,
) : WorkspaceRepository {

    override fun save(workspace: Workspace): Mono<Workspace> {
        val entity = R2dbcWorkspaceEntity(
            id = workspace.id,
            name = workspace.name,
            description = workspace.description,
        )
        return template.insert(entity).map { it.toDomain() }
    }

    override fun update(workspace: Workspace): Mono<Workspace> {
        return template.selectOne(
            org.springframework.data.relational.core.query.Query.query(
                org.springframework.data.relational.core.query.Criteria.where("id").`is`(workspace.id)
            ), R2dbcWorkspaceEntity::class.java
        ).flatMap { existing ->
            existing.name = workspace.name
            existing.description = workspace.description
            template.update(existing).map { it.toDomain() }
        }
    }

    override fun delete(id: UUID): Mono<Void> {
        return template.delete(
            org.springframework.data.relational.core.query.Query.query(
                org.springframework.data.relational.core.query.Criteria.where("id").`is`(id)
            ), R2dbcWorkspaceEntity::class.java
        ).then()
    }

    private fun R2dbcWorkspaceEntity.toDomain() = Workspace(id, name, description)
}
