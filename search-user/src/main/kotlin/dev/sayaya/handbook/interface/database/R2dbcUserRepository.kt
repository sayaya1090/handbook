package dev.sayaya.handbook.`interface`.database

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.util.*

@Repository
interface R2dbcUserRepository: R2dbcRepository<R2dbcUserWorkspaceProjection, UUID> {
    @Query("""
        SELECT 
            u.id, 
            u.provider,
            u.account,
            u.name, 
            u.created_at,
            u.last_modified_at,
            u.last_login_at,
            u.state,
            w.id as workspace_id, 
            w.name as workspace_name
        FROM "user" u
        JOIN group_member gm ON u.id = gm.member
        JOIN "group" g ON g.workspace = gm.workspace AND g.name = gm.group
        JOIN workspace w ON w.id = g.workspace
        WHERE u.id = :userId
        ORDER BY w.name
    """)
    fun findByUserId(userId: UUID): Flux<R2dbcUserWorkspaceProjection>
}