package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.usecase.type.TypeRepository
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.data.relational.core.query.Update
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import reactor.util.function.Tuples

@Repository
class R2dbcTypeRepository(private val template: R2dbcEntityTemplate, private val attributeRepo: R2dbcAttributeRepository): TypeRepository {
    @Transactional
    override fun save(type: Type): Mono<Type> = findOrCreate(type).flatMap { entity ->
        persistAttributes(entity, type)
    }.map(this::toDomain)
    private fun findOrCreate(type: Type): Mono<R2dbcTypeEntity> = findById(type.id)
        .map { entity -> entity.applyChanges(type) }
        .flatMap(::update)
        .switchIfEmpty(insert(type))
    private fun persistAttributes(entity: R2dbcTypeEntity, type: Type): Mono<Tuple2<R2dbcTypeEntity, List<Attribute>>> = attributeRepo.save(type, type.attributes)
        .map { Tuples.of(entity, it) }

    private fun findById(id: String): Mono<R2dbcTypeEntity> = template.selectOne(query(where("id").`is`(id)), R2dbcTypeEntity::class.java)
    private fun update(entity: R2dbcTypeEntity): Mono<R2dbcTypeEntity> {
        val update = Update.update("description", entity.description)
                           .set("parent", entity.parent)
                           .set("primitive", entity.primitive)
        return template.update(query(where("id").`is`(entity.id)), update, R2dbcTypeEntity::class.java).thenReturn(entity)
    }
    private fun insert(type: Type): Mono<R2dbcTypeEntity> = R2dbcTypeEntity(type.id, type.primitive).applyChanges(type).let(template::insert)
    private fun R2dbcTypeEntity.applyChanges(type: Type): R2dbcTypeEntity = apply {
        this.description = type.description
        this.parent = type.parent
        this.primitive = type.primitive
    }
    private fun toDomain(tuple: Tuple2<R2dbcTypeEntity, List<Attribute>>): Type = toDomain(tuple.t1, tuple.t2)
    private fun toDomain(entity: R2dbcTypeEntity, attributes: List<Attribute>): Type = Type (
        id = entity.id,
        parent = entity.parent,
        description = entity.description,
        primitive = entity.primitive,
        attributes = attributes
    )
}