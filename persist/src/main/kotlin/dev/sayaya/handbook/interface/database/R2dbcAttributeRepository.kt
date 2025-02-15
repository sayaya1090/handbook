package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.AttributeType
import dev.sayaya.handbook.domain.exception.MissingFieldException
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
class R2dbcAttributeRepository(private val template: R2dbcEntityTemplate) {
    fun findByType(type: String): Flux<Attribute> = template.select(query(where("type").`is`(type)), R2dbcAttributeEntity::class.java).map(::toDomain)

    private fun toDomain(entity: R2dbcAttributeEntity): Attribute = when(entity.attributeType) {
        AttributeType.Value -> Attribute.Companion.ValueAttribute(
            name=entity.id.name,
            description=entity.description,
            nullable=entity.nullable
        )
        AttributeType.Array -> Attribute.Companion.ArrayAttribute (
            name=entity.id.name,
            description=entity.description,
            nullable=entity.nullable,
            valueType = entity.valueType ?: throw MissingFieldException("Missing valueType for AttributeType.Array")
        )
        AttributeType.Map -> Attribute.Companion.MapAttribute(
            name=entity.id.name,
            description=entity.description,
            nullable=entity.nullable,
            keyType = entity.keyType ?: throw IllegalStateException(),
            valueType = entity.valueType ?: throw IllegalStateException()
        )
        AttributeType.File -> Attribute.Companion.FileAttribute(
            name=entity.id.name,
            description=entity.description,
            nullable=entity.nullable,
            extensions = entity.fileExtensions?.split(",")?.map { it.trim() }?.toSet() ?: throw IllegalStateException()
        )
        AttributeType.Document -> Attribute.Companion.DocumentAttribute(
            name=entity.id.name,
            description=entity.description,
            nullable=entity.nullable,
            referenceType = entity.referenceType ?: throw IllegalStateException()
        )
        else -> throw IllegalArgumentException("Unsupported AttributeType: '${entity.attributeType}'")
    }
}