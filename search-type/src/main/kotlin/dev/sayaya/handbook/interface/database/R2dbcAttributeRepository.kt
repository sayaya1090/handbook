package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.AttributeType
import dev.sayaya.handbook.domain.exception.MissingFieldException
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import java.util.*

@Repository
class R2dbcAttributeRepository(private val template: R2dbcEntityTemplate) {
    @Transactional(readOnly = true)
    fun findAllByTypeIds(types: List<UUID>): Mono<Map<UUID, List<Attribute>>> = template.select(query(where("type").`in`(types)), R2dbcAttributeEntity::class.java)
        .groupBy(R2dbcAttributeEntity::type, ::toDomain)
        .flatMap { grouped -> grouped.collectList().map { grouped.key() to it } }
        .collectMap({ it.first }, { it.second })

    private fun toDomain(entity: R2dbcAttributeEntity): Attribute = when(entity.attributeType) {
        AttributeType.Value -> Attribute.Companion.ValueAttribute(
            name=entity.id.name,
            description=entity.description,
            nullable=entity.nullable,
            inherited = false
        )
        AttributeType.Array -> Attribute.Companion.ArrayAttribute (
            name=entity.id.name,
            description=entity.description,
            nullable=entity.nullable,
            valueType = entity.valueType ?: throw MissingFieldException("Missing valueType for ArrayAttribute with name: ${entity.id.name}"),
            inherited = false
        )
        AttributeType.Map -> Attribute.Companion.MapAttribute(
            name=entity.id.name,
            description=entity.description,
            nullable=entity.nullable,
            keyType = entity.keyType ?: throw IllegalStateException("Missing keyType for MapAttribute with name: ${entity.id.name}"),
            valueType = entity.valueType ?: throw IllegalStateException("Missing valueType for MapAttribute with name: ${entity.id.name}"),
            inherited = false
        )
        AttributeType.File -> Attribute.Companion.FileAttribute(
            name=entity.id.name,
            description=entity.description,
            nullable=entity.nullable,
            extensions = entity.fileExtensions?.split(",")?.map { it.trim() }?.toSet() ?: throw IllegalStateException(),
            inherited = false
        )
        AttributeType.Document -> Attribute.Companion.DocumentAttribute(
            name=entity.id.name,
            description=entity.description,
            nullable=entity.nullable,
            referenceType = entity.referenceType ?: throw IllegalStateException(),
            inherited = false
        )
        else -> throw IllegalArgumentException("Unsupported AttributeType: '${entity.attributeType}'")
    }
}