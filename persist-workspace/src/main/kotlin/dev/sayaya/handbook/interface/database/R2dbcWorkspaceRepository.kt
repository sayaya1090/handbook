package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Workspace
import dev.sayaya.handbook.usecase.WorkspaceRepository
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Repository
class R2dbcWorkspaceRepository(private val template: R2dbcEntityTemplate): WorkspaceRepository {
    @Transactional
    override fun save(workspace: Workspace): Mono<Workspace> = insert(workspace).map(this::toDomain)

    private fun insert(workspace: Workspace): Mono<R2dbcWorkspaceEntity> = R2dbcWorkspaceEntity.of(
        id = workspace.id,
        name = workspace.name,
        description = workspace.description ?: ""
    ).let(template::insert)
    private fun toDomain(entity: R2dbcWorkspaceEntity): Workspace = Workspace(entity.id, entity.name, entity.description)
}