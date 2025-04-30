package dev.sayaya.handbook.`interface`.database

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
    override fun findByBaseTime(workspace: UUID, baseTime: Instant): Flux<TypeWithLayout> = template.databaseClient.sql(sql)
            .bind("baseTime", baseTime) // 바인딩된 baseTime 값
            .bind("workspace", workspace) // 바인딩된 workspace ID 값
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
        private val sql = """
            SELECT t.name, t.version, t.parent, 
            t.effective_at, t.expire_at, t.description, t.primitive, 
            l.x, l.y, l.width, l.height
            FROM type t 
            INNER JOIN layout_type l 
            ON t.workspace = l.workspace AND t.name = l.type AND t.version = l.version AND t.last=true
            INNER JOIN layout p
            ON l.layout = p.id
            AND p.effective_at <= :baseTime
            AND p.expire_at > :baseTime
            AND t.workspace = :workspace
        """.trimIndent()
        fun Short.unsigned(): UShort = (this + 32768).toUShort()
    }
}