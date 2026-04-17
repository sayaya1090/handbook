package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.AttributeType
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue

/**
 * R2DBC attribute 엔티티 → 도메인 [Attribute] 단방향 매퍼 (search-type 은 read-only).
 * JSONB 컬럼(`attribute_type`, `read_roles`, `write_roles`) 의 역직렬화를 담당.
 */
class AttributeEntityMapper(
    private val objectMapper: ObjectMapper,
) {
    fun toDomain(entity: R2dbcAttributeEntity): Attribute = Attribute(
        name = entity.name,
        order = entity.order,
        description = entity.description,
        type = objectMapper.readValue<AttributeType>(entity.attributeType.asString()),
        nullable = entity.nullable,
        inherited = entity.inherited,
        readRoles = objectMapper.readValue<List<String>>(entity.readRoles.asString()),
        writeRoles = objectMapper.readValue<List<String>>(entity.writeRoles.asString()),
    )
}
