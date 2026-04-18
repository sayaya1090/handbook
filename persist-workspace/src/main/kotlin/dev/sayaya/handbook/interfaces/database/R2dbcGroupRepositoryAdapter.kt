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
     * 운영 경로: 컨트롤러가 `@AuthenticationPrincipal UserAuthentication` 으로 주입받은
     * 객체를 `Principal` 로 업캐스트해 넘긴다. `UserAuthentication.getName()` 은 JWT
     * `name` 클레임(사람이 읽는 표시명, 예: "Sangjay Bien") 을 반환하므로
     * `UUID.fromString(principal.name)` 은 실패. UUID 는 `UserAuthentication.id`
     * (JWT `jti`) 에 들어있으므로 다운캐스트해서 읽는다.
     *
     * 테스트 경로: `Principal { UUID.randomUUID().toString() }` 람다로 name 에 UUID
     * 문자열을 직접 넣는 패턴이 integration 테스트에 존재 — 이 경우 그대로
     * `principal.name` 을 파싱. (`R2dbcWorkspaceCascadeIntegrationTest` 가 대표 사례)
     */
    private fun userUuid(principal: Principal): UUID = when (principal) {
        is UserAuthentication -> UUID.fromString(principal.id ?: error("UserAuthentication.id (JWT jti) is null"))
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
