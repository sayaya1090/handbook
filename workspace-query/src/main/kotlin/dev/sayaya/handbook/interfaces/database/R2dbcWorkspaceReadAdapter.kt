package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Workspace
import dev.sayaya.handbook.usecase.WorkspaceReadRepository
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

/**
 * [WorkspaceReadRepository] 의 R2DBC 읽기 전용 구현.
 *
 * **책임:** `workspace` 테이블에서 `SELECT` 만 수행하여 도메인 [Workspace] 로 매핑한다.
 *
 * **의존관계:**
 * - [R2dbcEntityTemplate] — Spring Data R2DBC, Reactive 데이터 접근
 *
 * **주의:** 연결은 `application.yml` 의 `options=-c default_transaction_read_only=on`
 * 으로 보호되어 실수로 writeable 메서드가 호출돼도 DB 레벨에서 거부된다.
 */
@Repository
class R2dbcWorkspaceReadAdapter(
    private val template: R2dbcEntityTemplate,
    private val databaseClient: DatabaseClient,
) : WorkspaceReadRepository {

    override fun findAll(): Flux<Workspace> =
        template.select(R2dbcWorkspaceEntity::class.java).all().map { it.toDomain() }

    override fun findById(id: UUID): Mono<Workspace> =
        template.selectOne(
            Query.query(Criteria.where("id").`is`(id)),
            R2dbcWorkspaceEntity::class.java,
        ).map { it.toDomain() }

    override fun findByUserSub(sub: UUID): Flux<Workspace> =
        databaseClient.sql(
            """
            SELECT DISTINCT w.id, w.name, w.description
              FROM workspace w
              JOIN group_member gm ON gm.workspace = w.id
             WHERE gm.member = :sub
             ORDER BY w.name
            """.trimIndent(),
        )
            .bind("sub", sub)
            .map { row, _ ->
                Workspace(
                    id = row.get("id", UUID::class.java)!!,
                    name = row.get("name", String::class.java)!!,
                    description = row.get("description", String::class.java),
                )
            }
            .all()

    private fun R2dbcWorkspaceEntity.toDomain() = Workspace(id, name, description)
}
