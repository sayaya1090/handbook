package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Layout
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.domain.TypeWithLayout
import dev.sayaya.handbook.usecase.LayoutRepository
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.time.Instant
import java.util.*

@Repository
class R2dbcTypeLayoutRepository(private val template: R2dbcEntityTemplate): LayoutRepository {
    override fun findAll(workspace: UUID): Flux<Layout> = template.databaseClient.sql(FIND_LAYOUT_SQL)
        .bind("workspace", workspace)
        .map { row ->
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

    override fun findByRange(workspace: UUID, effectDateTime: Instant, expireDateTime: Instant): Flux<TypeWithLayout> = template.databaseClient.sql(FIND_TYPE_LAYOUT_SQL)
        .bind("effectDateTime", effectDateTime)
        .bind("expireDateTime", expireDateTime)
        .bind("workspace", workspace)
        .map { row ->
            TypeWithLayout(
                type = Type(
                    id = row.get("name", String::class.java)!!,
                    version = row.get("version", String::class.java)!!,
                    effectDateTime = row.get("effective_at", Instant::class.java)!!,
                    expireDateTime = row.get("expire_at", Instant::class.java)!!,
                    description = row.get("description", String::class.java) ?: "",
                    primitive = row.get("primitive", Boolean::class.java) ?: false,
                    attributes = emptyList(),
                    parent = row.get("parent", String::class.java),
                ), x = row.get("x", Short::class.java)!!.unsigned(),
                y = row.get("y", Short::class.java)!!.unsigned(),
                width = row.get("width", Short::class.java)!!.unsigned(),
                height = row.get("height", Short::class.java)!!.unsigned()
            )
        }.all()

    companion object {
        private val FIND_LAYOUT_SQL = """
            SELECT t.effective_at, t.expire_at FROM type t WHERE t.workspace = :workspace AND t.last=true
        """.trimIndent()
        private val FIND_TYPE_LAYOUT_SQL = """
            SELECT t.name, t.version, t.parent, 
            t.effective_at, t.expire_at, t.description, t.primitive, 
            t.x, t.y, t.width, t.height
            FROM type t 
            WHERE t.effective_at < :expireDateTime
            AND t.expire_at >= :effectDateTime
            AND t.workspace = :workspace
        """.trimIndent()
        fun Short.unsigned(): UShort = (this + 32768).toUShort()
    }
}