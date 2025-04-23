package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Group
import dev.sayaya.handbook.domain.Workspace
import reactor.core.publisher.Mono
import java.security.Principal

interface GroupRepository {
    // 그룹을 생성하고 생성자를 그룹에 배정한다.
    fun createAndAssign(workspace: Workspace, creator: Principal, name: String, description: String?): Mono<Group>
}