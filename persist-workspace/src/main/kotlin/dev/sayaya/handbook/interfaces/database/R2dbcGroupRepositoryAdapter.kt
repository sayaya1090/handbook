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
            member = userUuid(creator),
        )
        return template.insert(groupEntity)
            .delayUntil { template.insert(memberEntity) }
            .map { Group(UUID.randomUUID(), workspace.id, name, description) }
    }

    override fun addMember(workspaceId: UUID, principal: Principal): Mono<Void> {
        val memberEntity = R2dbcGroupMemberEntity(
            workspace = workspaceId,
            group = GROUP_MEMBER,
            member = userUuid(principal),
        )
        return template.insert(memberEntity).then()
    }

    /**
     * Principal 에서 사용자 UUID 를 추출한다.
     *
     * ### 우선순위 (Phase 1a — 2026-04-18)
     * `UserAuthentication` 인 경우 다음 순서로 폴백한다:
     *   1. `sub` — 사용자 식별자 (JWT `sub` 클레임, 재발급 불변). 신규 경로.
     *   2. `id`  — Phase 1a 이전 토큰에서는 사용자 UUID 가 `jti` 에 담겨 있었으므로 폴백.
     *              이후 토큰은 매 발행 고유 토큰 ID 를 담으나 현재는 양립 기간.
     *   3. 둘 다 null 이면 `principal.name` 을 마지막 폴백으로 파싱. 실패 시 예외.
     *
     * **테스트 경로:** `Principal { UUID.randomUUID().toString() }` 람다는 `UserAuthentication`
     * 이 아니므로 else 분기로 빠져 `principal.name` 을 그대로 파싱한다
     * (`R2dbcWorkspaceCascadeIntegrationTest` 등).
     */
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

    /**
     * `group_member` 먼저 삭제한 뒤 `group` row 를 삭제한다. FK 가 물리적으로 선언되어
     * 있지 않더라도 의미론적 참조 순서를 보존한다.
     */
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
