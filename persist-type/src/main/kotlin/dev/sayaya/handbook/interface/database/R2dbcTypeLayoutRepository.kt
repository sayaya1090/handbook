package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Layout
import dev.sayaya.handbook.domain.TypeWithLayout
import dev.sayaya.handbook.usecase.type.TypeLayoutRepository
import io.r2dbc.spi.Statement
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class R2dbcTypeLayoutRepository(private val template: R2dbcEntityTemplate): TypeLayoutRepository {
    override fun saveAll(layout: Layout, typeWithLayout: List<TypeWithLayout>): Mono<List<TypeWithLayout>> = typeWithLayout.toEntity(layout).let {
        template.saveAll(it)
    }.thenReturn(typeWithLayout)
    private fun R2dbcEntityTemplate.saveAll(entities: List<R2dbcTypeLayoutEntity>): Mono<Void> {
        val sql = """
                INSERT INTO layout_type (
                    workspace, layout, type, version, x, y, width, height
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(workspace, layout, type, version) 
                DO UPDATE SET x=excluded.x, y=excluded.y, width=excluded.width, height=excluded.height
            """.trimIndent()
        return databaseClient.inConnection { connection ->
            val statement = connection.createStatement(sql)
            entities.forEach { entity -> statement.bind(entity).add() }
            Flux.from(statement.execute()).then()
        }
    }
    private fun Statement.bind(entity: R2dbcTypeLayoutEntity) = bind(0, entity.workspace)
        .bind(1, entity.layout)
        .bind(2, entity.type)
        .bind(3, entity.version)
        .bind(4, entity.x)
        .bind(5, entity.y)
        .bind(6, entity.width)
        .bind(7, entity.height)
    private fun List<TypeWithLayout>.toEntity(layout: Layout) = this.map {
        R2dbcTypeLayoutEntity (
            workspace = layout.workspace,
            layout = layout.id,
            type = it.type.id,
            version = it.type.version,
            x = it.x, y = it.y,
            width = it.width, height = it.height
        )
    }
}