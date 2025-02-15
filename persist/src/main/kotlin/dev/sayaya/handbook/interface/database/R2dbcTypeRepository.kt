package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.usecase.type.TypeRepository
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.data.relational.core.query.Update
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Repository
class R2dbcTypeRepository(private val template: R2dbcEntityTemplate, private val attributeRepo: R2dbcAttributeRepository): TypeRepository {
    @Transactional
    override fun save(type: Type): Mono<Type> = findById(type.id).map { entity -> entity.applyChanges(type) }.flatMap(::update)
        .switchIfEmpty(insert(type))
        .flatMap(::toDomain)
    private fun findById(id: String): Mono<R2dbcTypeEntity> = template.selectOne(query(where("id").`is`(id)), R2dbcTypeEntity::class.java)
    private fun update(entity: R2dbcTypeEntity): Mono<R2dbcTypeEntity> {
        val update = Update.update("description", entity.description)
            .set("parent", entity.parent)
            .set("primitive", entity.primitive)
        return template.update(query(where("id").`is`(entity.id)), update, R2dbcTypeEntity::class.java).thenReturn(entity)
    }
    private fun insert(type: Type): Mono<R2dbcTypeEntity> = R2dbcTypeEntity(type.id, type.primitive).applyChanges(type).let(template::insert)
    private fun R2dbcTypeEntity.applyChanges(type: Type): R2dbcTypeEntity {
        this.description = type.description
        this.parent = type.parent
        this.primitive = type.primitive
        return this
    }
    private fun toDomain(entity: R2dbcTypeEntity): Mono<Type> = attributeRepo.findByType(entity.id).collectList().map { attributes->
        Type(
            id = entity.id,
            parent = entity.parent,
            description = entity.description,
            primitive = entity.primitive,
            attributes = attributes
        )
    }
}