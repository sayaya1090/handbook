package dev.sayaya.handbook.interfaces.database

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.*

/**
 * 속성 테이블. 타입별 속성을 별도 테이블에 저장하여 속성 기반 검색을 지원한다.
 * attribute_type은 JSONB 컬럼으로 AttributeType의 다형적 구조를 저장한다.
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
    /** JSONB — AttributeType의 직렬화된 형태 */
    @Column("attribute_type") val attributeType: String,
    val nullable: Boolean,
    val inherited: Boolean,
)
