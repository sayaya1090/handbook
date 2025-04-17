package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.User
import dev.sayaya.handbook.domain.Workspace
import dev.sayaya.handbook.usecase.UserRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*

@Component
class R2dbcUserRepositoryDelegate(val repo: R2dbcUserRepository): UserRepository {
    override fun find(id: UUID): Mono<User> = repo.findByUserId(id).collectList().mapNotNull { it.toDomain() }
    private fun List<R2dbcUserWorkspaceProjection>.toDomain(): User? = this.firstOrNull()?.let { firstElement ->
        User (
            id = firstElement.id,
            name = firstElement.name,
            workspaces = map {
                Workspace.Companion.Simple(it.workspaceId, it.workspaceName)
            }
        )
    }
}