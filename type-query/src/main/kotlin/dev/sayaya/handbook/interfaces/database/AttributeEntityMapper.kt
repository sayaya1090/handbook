package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.AttributeType
import org.springframework.stereotype.Component

/**
 * [R2dbcAttributeEntity]와 [Attribute] 도메인 객체 간의 변환을 담당하는 매퍼.
 */
@Component
class AttributeEntityMapper {
    fun toDomain(entity: R2dbcAttributeEntity): Attribute {
        return Attribute.create(
            entity.id.toString(),
            entity.name,
            entity.order.toInt(),
            // Simple string to AttributeType - in reality more complex parsing might be needed
            AttributeType.text() // Placeholder for now
        ).apply {
            description(entity.description)
            nullable(entity.nullable)
        }
    }
}
