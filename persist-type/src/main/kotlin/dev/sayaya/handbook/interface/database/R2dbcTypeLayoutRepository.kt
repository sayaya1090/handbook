package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Layout
import dev.sayaya.handbook.domain.TypeWithLayout
import dev.sayaya.handbook.usecase.type.TypeLayoutRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class R2dbcTypeLayoutRepository(override val databaseClient: DatabaseClient): TypeLayoutRepository, BatchRepository<R2dbcTypeLayoutEntity> {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
    override fun saveAll(layout: Layout, typeWithLayout: List<TypeWithLayout>): Mono<List<TypeWithLayout>> = prune(layout, typeWithLayout).flatMapMany {
        typeWithLayout.toEntity(layout).let {
            saveAll(it).doOnError { error -> log.error("Batch execution failed inside inConnection!", error) }
        }
    }.collectList().thenReturn(typeWithLayout)
    private fun prune(layout: Layout, typeWithLayout: List<TypeWithLayout>): Mono<Long> {
        val typeVersionPairs = typeWithLayout.joinToString(", ") {
            "(${escapeSql(it.type.id)}, ${escapeSql(it.type.version)})"
        }
        val query = PRUNE_LAYOUT_TYPES_TEMPLATE_SQL.replace("%s", typeVersionPairs)
        return databaseClient.sql(query)
            .bind("workspace", layout.workspace)
            .bind("layout", layout.id)
            .fetch().rowsUpdated()
    }
    override fun insertInto(): String = INSERT_LAYOUT_TYPE_SQL
    override fun R2dbcTypeLayoutEntity.toCsv(): String = """
        ${escapeSql(workspace.toString())},
        ${escapeSql(layout.toString())},
        ${escapeSql(type)},
        ${escapeSql(version)},
        $x, $y, $width, $height
    """.trimIndent()
    override fun condition(): String = CONDITION_ON_CONFLICT_SQL
    private fun List<TypeWithLayout>.toEntity(layout: Layout) = this.map {
        R2dbcTypeLayoutEntity(
            workspace = layout.workspace,
            layout = layout.id,
            type = it.type.id,
            version = it.type.version,
            x = it.x.signed(), y = it.y.signed(),
            width = it.width.signed(), height = it.height.signed()
        )
    }
    companion object {
        private const val INSERT_LAYOUT_TYPE_SQL = """
            INSERT INTO layout_type (workspace, layout, type, version, x, y, width, height) 
        """
        private const val CONDITION_ON_CONFLICT_SQL = """
            ON CONFLICT(workspace, layout, type, version) 
            DO UPDATE SET x=excluded.x, y=excluded.y, width=excluded.width, height=excluded.height
        """
        private const val PRUNE_LAYOUT_TYPES_TEMPLATE_SQL = """
            DELETE FROM layout_type
            WHERE workspace = :workspace AND layout = :layout
            AND (type, version) NOT IN (%s) 
        """

        fun UShort.signed(): Short = (toInt() - 32768).toShort()
    }
}