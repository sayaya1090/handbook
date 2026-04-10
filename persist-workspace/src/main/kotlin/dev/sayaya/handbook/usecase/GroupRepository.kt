package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Group
import dev.sayaya.handbook.domain.Workspace
import reactor.core.publisher.Mono
import java.security.Principal

interface GroupRepository {
    fun createAndAssign(workspace: Workspace, creator: Principal, name: String, description: String?): Mono<Group>
}
