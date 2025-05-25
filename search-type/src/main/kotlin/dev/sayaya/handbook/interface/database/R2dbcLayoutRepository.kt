package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Layout
import dev.sayaya.handbook.usecase.LayoutRepository
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.time.Instant
import java.util.*

@Repository
class R2dbcLayoutRepository(private val client: DatabaseClient): LayoutRepository {
    override fun findAll(workspace: UUID): Flux<Layout> = client.sql(FIND_LAYOUT_SQL)
        .bind("workspace", workspace).map { row ->
            val effectDateTime = row.get("effective_at", Instant::class.java)!!
            val expireDateTime = row.get("expire_at", Instant::class.java)!!
            effectDateTime to expireDateTime
        }.all().collectList().flatMapIterable { list ->
            val set = TreeSet<Instant>()
            list.forEach { (effectDateTime, expireDateTime) ->
                set.add(effectDateTime)
                set.add(expireDateTime)
            }
            if (set.size < 2) emptyList()
            else set.toList().zipWithNext().map { (start, end) ->
                Layout(
                    workspace = workspace,
                    effectDateTime = start,
                    expireDateTime = end
                )
            }
        }

    companion object {
        private val FIND_LAYOUT_SQL = """
            SELECT t.effective_at, t.expire_at FROM type t WHERE t.workspace = :workspace AND t.last=true
        """.trimIndent()
    }
}