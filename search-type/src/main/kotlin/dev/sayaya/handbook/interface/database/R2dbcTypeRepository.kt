package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Layout
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.usecase.TypeRepository
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.time.Instant
import java.util.*

@Repository
class R2dbcTypeRepository(private val template: R2dbcEntityTemplate): TypeRepository {
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

    override fun findByRange(workspace: UUID, effectDateTime: Instant, expireDateTime: Instant): Flux<Type> =
        query(
            where("workspace").`is`(workspace)
                .and("effective_at").lessThan(expireDateTime)
                .and("expire_at").greaterThan(effectDateTime)
                .and("last").`is`(true)
        ).let { template.select(it, R2dbcTypeEntity::class.java) }
        .map { row -> row.toDomain() }
    private fun R2dbcTypeEntity.toDomain(): Type = Type(
        id = name,
        version = version,
        effectDateTime = effectDateTime,
        expireDateTime = expireDateTime,
        description = description,
        primitive = primitive,
        attributes = emptyList(),
        parent = parent,
        x = x.unsigned(),
        y = y.unsigned(),
        width = width.unsigned(),
        height = height.unsigned()
    )

    companion object {
        private val FIND_LAYOUT_SQL = """
            SELECT t.effective_at, t.expire_at FROM type t WHERE t.workspace = :workspace AND t.last=true
        """.trimIndent()
        fun Short.unsigned(): UShort = (this + 32768).toUShort()
    }
}