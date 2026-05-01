package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Group
import dev.sayaya.handbook.domain.User
import dev.sayaya.handbook.usecase.GroupReadRepository
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.util.*

/**
 * [GroupReadRepository] 의 R2DBC 읽기 전용 구현.
 *
 * **책임:** 워크스페이스 내 그룹 목록 조회 및 특정 그룹의 멤버(User) 목록 조회를 담당한다.
 *
 * **의존관계:**
 * - [R2dbcEntityTemplate] — Spring Data R2DBC 를 이용한 엔티티 조회
 * - [DatabaseClient] — 조인이 포함된 멤버 목록 조회를 위한 로우 SQL 실행
 */
@Repository
class R2dbcGroupReadAdapter(
    private val template: R2dbcEntityTemplate,
    private val databaseClient: DatabaseClient
) : GroupReadRepository {

    override fun findAllByWorkspace(workspaceId: UUID): Flux<Group> {
        val criteria = Query.query(Criteria.where("workspace").`is`(workspaceId))
        return template.select(criteria, R2dbcGroupEntity::class.java)
            .map { Group.create(it.id.toString(), it.workspace.toString(), it.name, it.description) }
    }

    override fun findMembersByGroup(workspaceId: UUID, groupId: UUID): Flux<User> {
        return databaseClient.sql(
            """
            SELECT u.id, u.username, u.email
              FROM users u
              JOIN group_member gm ON gm.member = u.id
             WHERE gm.workspace = :ws AND gm.group = :gid
             ORDER BY u.username
            """.trimIndent()
        )
            .bind("ws", workspaceId)
            .bind("gid", groupId)
            .map { row, _ ->
                User.create(
                    row.get("id", UUID::class.java)!!.toString(),
                    row.get("username", String::class.java)!!,
                    row.get("email", String::class.java)
                )
            }
            .all()
    }
}
