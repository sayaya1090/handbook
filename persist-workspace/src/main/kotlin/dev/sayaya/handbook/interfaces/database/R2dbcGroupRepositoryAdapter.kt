package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Group
import dev.sayaya.handbook.domain.Workspace
import dev.sayaya.handbook.interfaces.authentication.UserAuthentication
import dev.sayaya.handbook.usecase.GroupRepository
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
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
 */
@Repository
class R2dbcGroupRepositoryAdapter(
    private val template: R2dbcEntityTemplate,
) : GroupRepository {

    override fun save(group: Group): Mono<Group> {
        val entity = R2dbcGroupEntity(
            id = group.id,
            workspace = group.workspace,
            name = group.name,
            description = group.description
        )
        return template.insert(entity)
            .map { Group(it.id!!, it.workspace, it.name, it.description) }
    }

    override fun delete(workspaceId: UUID, groupId: UUID): Mono<Void> {
        val criteria = Query.query(
            Criteria.where("workspace").`is`(workspaceId)
                .and("id").`is`(groupId)
        )
        return template.delete(criteria, R2dbcGroupEntity::class.java).then()
    }

    override fun createAndAssign(workspace: Workspace, creator: Principal, name: String, description: String?): Mono<Group> {
        val groupId = UUID.randomUUID()
        val groupEntity = R2dbcGroupEntity(
            id = groupId,
            workspace = workspace.id,
            name = name,
            description = description
        )
        val memberEntity = R2dbcGroupMemberEntity(
            id = UUID.randomUUID(),
            workspace = workspace.id,
            group = groupId,
            member = userUuid(creator),
        )
        return template.insert(groupEntity)
            .delayUntil { template.insert(memberEntity) }
            .map { Group(groupId, workspace.id, name, description) }
    }

    override fun addMember(workspaceId: UUID, principal: Principal): Mono<Void> {
        // "Member" 그룹을 찾거나 없으면 생성해야 함 (Phase 1 에서는 기존 join 로직 유지하되 ID 기반으로 보완 필요)
        // 현재는 "Member" 그룹이 이미 존재한다고 가정하고 검색 후 추가하거나, 
        // 워크스페이스 생성 시 Admin 과 함께 Member 그룹도 자동 생성하는 것이 안전함.
        val criteria = Query.query(Criteria.where("workspace").`is`(workspaceId).and("name").`is`(GROUP_MEMBER))
        return template.selectOne(criteria, R2dbcGroupEntity::class.java)
            .flatMap { group ->
                val memberEntity = R2dbcGroupMemberEntity(
                    id = UUID.randomUUID(),
                    workspace = workspaceId,
                    group = group.id!!,
                    member = userUuid(principal),
                )
                template.insert(memberEntity)
            }.then()
    }

    override fun addMember(groupId: UUID, userId: UUID): Mono<Void> {
        // workspaceId 를 알기 위해 group 을 먼저 조회
        val criteria = Query.query(Criteria.where("id").`is`(groupId))
        return template.selectOne(criteria, R2dbcGroupEntity::class.java)
            .flatMap { group ->
                val memberEntity = R2dbcGroupMemberEntity(
                    id = UUID.randomUUID(),
                    workspace = group.workspace,
                    group = groupId,
                    member = userId,
                )
                template.insert(memberEntity)
            }.then()
    }

    override fun removeMember(groupId: UUID, userId: UUID): Mono<Void> {
        val criteria = Query.query(
            Criteria.where("group").`is`(groupId)
                .and("member").`is`(userId)
        )
        return template.delete(criteria, R2dbcGroupMemberEntity::class.java).then()
    }

    private fun userUuid(principal: Principal): UUID = when (principal) {
        is UserAuthentication -> {
            val raw = principal.sub
                ?: principal.id
                ?: principal.name
                ?: error("UserAuthentication has no sub/id/name to derive user UUID")
            UUID.fromString(raw)
        }
        else -> UUID.fromString(principal.name)
    }

    override fun deleteByWorkspace(workspaceId: UUID): Mono<Void> {
        val criteria = Query.query(Criteria.where("workspace").`is`(workspaceId))
        return template.delete(criteria, R2dbcGroupMemberEntity::class.java)
            .then(template.delete(criteria, R2dbcGroupEntity::class.java))
            .then()
    }

    companion object {
        const val GROUP_MEMBER = "Member"
    }
}
