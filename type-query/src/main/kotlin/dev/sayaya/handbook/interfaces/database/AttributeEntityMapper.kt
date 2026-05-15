package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.AttributeType
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue

/**
 * [R2dbcAttributeEntity]와 [Attribute] 도메인 객체 간의 변환을 담당하는 매퍼.
 */
@Component
class AttributeEntityMapper(private val objectMapper: ObjectMapper) {
    fun toDomain(entity: R2dbcAttributeEntity): Attribute {
        val type: AttributeType = entity.attributeType.asString()?.let {
            objectMapper.readValue(it)
        } ?: AttributeType.text()
        
        return Attribute.create(
            entity.id.toString(),
            entity.name,
            entity.order.toInt(),
            type
        ).apply {
            description(entity.description)
            nullable(entity.nullable)
        }
    }
}
