package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.AttributeType
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.domain.exception.MissingFieldException
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.data.relational.core.query.Update
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class R2dbcAttributeRepository(private val template: R2dbcEntityTemplate) {
    fun findByType(type: String): Flux<Attribute> = findAllByTypeId(type).map(::toDomain)
    private fun findAllByTypeId(type: String): Flux<R2dbcAttributeEntity> = template.select(query(where("type").`is`(type)), R2dbcAttributeEntity::class.java)
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
            valueType = entity.valueType ?: throw MissingFieldException("Missing valueType for ArrayAttribute with name: ${entity.id.name}")
        )
        AttributeType.Map -> Attribute.Companion.MapAttribute(
            name=entity.id.name,
            description=entity.description,
            nullable=entity.nullable,
            keyType = entity.keyType ?: throw IllegalStateException("Missing keyType for MapAttribute with name: ${entity.id.name}"),
            valueType = entity.valueType ?: throw IllegalStateException("Missing valueType for MapAttribute with name: ${entity.id.name}")
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
    @Transactional
    fun save(type: Type, attributes: List<Attribute>): Mono<List<Attribute>> = findAllByTypeId(type.id)  // Step 1: 기존 데이터 조회
        .collectList()
        .flatMap { currentAttributes ->
            val attributesToInsert = attributes.filter { incoming ->
                currentAttributes.none { it.name == incoming.name } // 기존 목록에 없는 새로운 데이터
            }
            val attributesToUpdate = attributes.filter { incoming ->
                currentAttributes.any { existing -> existing.name == incoming.name } // 변경된 데이터
            }
            val attributesToDelete = currentAttributes.filter { existing ->
                attributes.none { it.name == existing.name } // 새 목록에 없는 기존 데이터
            }
            // Step 2: 삽입, 업데이트, 삭제 작업 처리
            val insertFlux = Flux.fromIterable(attributesToInsert).flatMap { insert(type, it) }
            val updateFlux = Flux.fromIterable(attributesToUpdate).flatMap { update(type, it) }
            val deleteFlux = Flux.fromIterable(attributesToDelete).flatMap(::delete)

            // Step 3: 모든 작업 실행 후 최종 데이터 반환
            Flux.concat(insertFlux, updateFlux, deleteFlux).thenMany(findByType(type.id)).collectList()
        }
    private fun Attribute.toEntity(type: Type): R2dbcAttributeEntity = when(this) {
        is Attribute.Companion.ValueAttribute   -> R2dbcAttributeEntity(
            type = type.id,
            name = name,
            attributeType = AttributeType.Value,
            keyType = null,
            valueType = null,
            referenceType = null,
            fileExtensions = null,
            description = description,
            nullable = nullable
        )
        is Attribute.Companion.ArrayAttribute   -> R2dbcAttributeEntity(
            type = type.id,
            name = name,
            attributeType = AttributeType.Array,
            keyType = null,
            valueType = valueType,
            referenceType = null,
            fileExtensions = null,
            description = description,
            nullable = nullable
        )
        is Attribute.Companion.MapAttribute     -> R2dbcAttributeEntity(
            type = type.id,
            name = name,
            attributeType = AttributeType.Map,
            keyType = keyType,
            valueType = valueType,
            referenceType = null,
            fileExtensions = null,
            description = description,
            nullable = nullable
        )
        is Attribute.Companion.DocumentAttribute-> R2dbcAttributeEntity(
            type = type.id,
            name = name,
            attributeType = AttributeType.Document,
            keyType = null,
            valueType = null,
            referenceType = referenceType,
            fileExtensions = null,
            description = description,
            nullable = nullable
        )
        is Attribute.Companion.FileAttribute    -> R2dbcAttributeEntity(
            type = type.id,
            name = name,
            attributeType = AttributeType.File,
            keyType = null,
            valueType = null,
            referenceType = null,
            fileExtensions = extensions.map{ it.trim() }.joinToString(","),
            description = description,
            nullable = nullable
        )
        else -> throw IllegalArgumentException("Unsupported AttributeType: '${type}'")
    }
    private fun insert(type: Type, attribute: Attribute): Mono<R2dbcAttributeEntity> = attribute.toEntity(type).let { template.insert(it) }
    private fun update(type: Type, attribute: Attribute): Mono<R2dbcAttributeEntity> = attribute.toEntity(type).let { entity ->
        val update = Update.update("attribute_type", entity.attributeType)
            .set("key_type", entity.keyType)
            .set("value_type", entity.valueType)
            .set("reference_type", entity.referenceType)
            .set("file_extensions", entity.fileExtensions)
            .set("description", entity.description)
            .set("nullable", entity.nullable)
        return template.update(
            query(where("type").`is`(type.id).and("name").`is`(attribute.name)),
            update, R2dbcAttributeEntity::class.java).thenReturn(entity)
    }
    private fun delete(entity: R2dbcAttributeEntity): Mono<R2dbcAttributeEntity> = template.delete(
        query(where("type").`is`(entity.type).and("name").`is`(entity.name)),
        R2dbcAttributeEntity::class.java
    ).thenReturn(entity)
}