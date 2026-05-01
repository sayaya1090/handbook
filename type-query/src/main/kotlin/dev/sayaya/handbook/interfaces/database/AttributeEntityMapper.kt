package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.AttributeType
import tools.jackson.databind.ObjectMapper

/**
 * R2DBC attribute 엔티티 → 도메인 [Attribute] 단방향 매퍼 (type-query 은 read-only).
 * JSONB 컬럼(`attribute_type`, `read_roles`, `write_roles`) 의 역직렬화를 담당.
 */
class AttributeEntityMapper(
    private val objectMapper: ObjectMapper,
) {
    fun toDomain(entity: R2dbcAttributeEntity): Attribute = Attribute().apply {
        this.name = entity.name
        this.order = entity.order.toInt()
        this.description(entity.description)
        this.type = objectMapper.readValue(entity.attributeType.asString(), AttributeType::class.java)
        this.nullable(entity.nullable)
        this.inherited(entity.inherited)
        this.readRoles(objectMapper.readValue(entity.readRoles.asString(), Array<String>::class.java))
        this.writeRoles(objectMapper.readValue(entity.writeRoles.asString(), Array<String>::class.java))
    }
}
