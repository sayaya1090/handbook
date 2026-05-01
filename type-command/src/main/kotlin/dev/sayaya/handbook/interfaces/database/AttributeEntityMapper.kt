package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.AttributeType
import io.r2dbc.postgresql.codec.Json
import tools.jackson.databind.ObjectMapper
import java.util.*

/**
 * [R2dbcAttributeEntity]와 도메인 [Attribute] 간의 매핑을 담당하는 매퍼.
 *
 * **책임:** 속성 엔티티 → 도메인 변환(toDomain)과 도메인 → 엔티티 변환(toEntity)을 제공한다.
 * JSONB 직렬화/역직렬화가 필요한 attributeType, readRoles, writeRoles 필드를 처리한다.
 *
 * **의존관계:**
 * - [ObjectMapper] — attributeType(JSONB)과 roles(JSONB) 직렬화/역직렬화
 *
 * **주의:** ObjectMapper는 snake_case 네이밍 전략이 적용된 인스턴스를 사용해야 한다.
 */
class AttributeEntityMapper(
    private val objectMapper: ObjectMapper,
) {
    /**
     * R2DBC 속성 엔티티를 도메인 [Attribute]로 변환한다.
     *
     * @param entity 변환할 속성 엔티티
     * @return 도메인 Attribute 객체
     */
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

    /**
     * 도메인 [Attribute]를 R2DBC 속성 엔티티로 변환한다.
     *
     * @param typeId 소속 타입 ID
     * @param typeVersion 소속 타입 버전
     * @param workspace 워크스페이스 ID
     * @param attr 변환할 도메인 Attribute
     * @return R2DBC 속성 엔티티
     */
    fun toEntity(typeId: String, typeVersion: String, workspace: UUID, attr: Attribute): R2dbcAttributeEntity =
        R2dbcAttributeEntity(
            typeId = typeId,
            typeVersion = typeVersion,
            workspace = workspace,
            name = attr.name,
            order = attr.order.toShort(),
            description = attr.description(),
            attributeType = Json.of(objectMapper.writeValueAsString(attr.type)),
            nullable = attr.nullable,
            inherited = attr.inherited,
            readRoles = Json.of(objectMapper.writeValueAsString(attr.readRoles)),
            writeRoles = Json.of(objectMapper.writeValueAsString(attr.writeRoles)),
        )
}
