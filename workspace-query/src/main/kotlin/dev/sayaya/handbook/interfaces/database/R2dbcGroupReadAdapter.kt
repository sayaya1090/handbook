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
