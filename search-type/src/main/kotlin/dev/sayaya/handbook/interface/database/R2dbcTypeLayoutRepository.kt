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
            .mapValue(R2dbcTypeLayoutEntity::class.java) // R2dbcTypeLayoutEntity로 매핑
            .all()
            .map { it.toDomain() }

    companion object {
        private val sql = """
            SELECT t.workspace, t.id, t.name, t.version, t.parent, 
            t.effective_at, t.expire_at, t.description, t.primitive, t.last,
            t.created_at, t.created_by,
            l.layout, l.x, l.y, l.width, l.height
            FROM type t 
            INNER JOIN layout_type l
            ON t.workspace = l.workspace AND t.name = l.type AND t.version = l.version AND t.last=true
            INNER JOIN layout p
            ON l.layout = p.id
            AND p.effective_at <= :baseTime
            AND p.expire_at > :baseTime
            AND t.workspace = :workspace
        """.trimIndent()
        fun R2dbcTypeLayoutEntity.toDomain(): TypeWithLayout = TypeWithLayout (
            type = Type(
                id = this.name,
                version = this.version,
                effectDateTime = this.effectDateTime,
                expireDateTime = this.expireDateTime,
                description = this.description,
                primitive = this.primitive,
                attributes = emptyList(),
                parent = this.parent,
            ), x = this.x.unsigned(),
            y = this.y.unsigned(),
            width = this.width.unsigned(),
            height = this.height.unsigned()
        )
        fun Short.unsigned(): UShort = (this + 32768).toUShort()
    }
}