package dev.sayaya.handbook.interfaces.database

import io.r2dbc.postgresql.codec.Json
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

/**
 * `type_attributes` 테이블의 **읽기 전용 투영** — type-query 전용.
 * type-command 엔티티와 동일 스키마, 쓰기 메서드 없음. JSONB 컬럼은 그대로 가져와
 * adapter 가 ObjectMapper 로 도메인 변환한다.
 */
@Table("type_attributes")
data class R2dbcAttributeEntity(
    @Id val id: UUID? = null,
    @Column("type_id") val typeId: String,
    @Column("type_version") val typeVersion: String,
    val workspace: UUID,
    val name: String,
    @Column("attr_order") val order: Short,
    val description: String?,
    @Column("attribute_type") val attributeType: Json,
    val nullable: Boolean,
    val inherited: Boolean,
    @Column("read_roles") val readRoles: Json = Json.of("[]"),
    @Column("write_roles") val writeRoles: Json = Json.of("[]"),
)
