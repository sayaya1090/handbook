package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Group
import dev.sayaya.handbook.domain.Workspace
import dev.sayaya.handbook.usecase.GroupRepository
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.security.Principal
import java.util.*

/**
 * [GroupRepository] 포트의 R2DBC 어댑터.
 *
 * **책임:** 그룹 생성/멤버 배정 및 기존 그룹에 멤버 추가를 R2DBC로 처리한다.
 *
 * **의존관계:**
 * - [R2dbcEntityTemplate] — R2DBC 엔티티 삽입/조회
 *
 * **주의:** addMember는 기본 "Member" 그룹에 사용자를 추가한다.
 * 그룹이 존재하지 않으면 자동 생성 후 멤버를 추가한다.
 */
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

    override fun addMember(workspaceId: UUID, principal: Principal): Mono<Void> {
        val memberEntity = R2dbcGroupMemberEntity(
            workspace = workspaceId,
            group = GROUP_MEMBER,
            member = UUID.fromString(principal.name),
        )
        return template.insert(memberEntity).then()
    }

    companion object {
        const val GROUP_MEMBER = "Member"
    }
}
