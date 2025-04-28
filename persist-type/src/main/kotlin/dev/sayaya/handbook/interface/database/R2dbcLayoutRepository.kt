package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Layout
import dev.sayaya.handbook.usecase.type.LayoutRepository
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.*

@Repository
class R2dbcLayoutRepository(private val template: R2dbcEntityTemplate): LayoutRepository {
    override fun findById(workspace: UUID, id: UUID): Mono<Layout> = template.select(query(
        where("workspace").`is`(workspace).and("id").`is`(id)), R2dbcLayoutEntity::class.java
    ).single().map(R2dbcLayoutRepository::toDomain)
    override fun save(layout: Layout): Mono<Layout> = layout.toEntity().let(template::insert).map(R2dbcLayoutRepository::toDomain)
    companion object {
        private fun toDomain(entity: R2dbcLayoutEntity): Layout = Layout(entity.workspace, entity.id).apply {
            effectDateTime = entity.effectDateTime
            expireDateTime = entity.expireDateTime
        }
        private fun Layout.toEntity(): R2dbcLayoutEntity = R2dbcLayoutEntity.of(
            workspace = workspace,
            id = id,
            effectDateTime = effectDateTime,
            expireDateTime = expireDateTime
        )
    }

}