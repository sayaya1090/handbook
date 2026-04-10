package dev.sayaya.handbook.interfaces.database

import io.r2dbc.postgresql.codec.Json
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.*

/**
 * type_attributes 테이블에 매핑되는 R2DBC 엔티티.
 *
 * **책임:** 타입별 속성을 별도 테이블에 저장하여 속성 기반 검색을 지원한다.
 * attribute_type 컬럼은 JSONB로 [AttributeType][dev.sayaya.handbook.domain.AttributeType]의 다형적 구조를 저장한다.
 *
 * **의존관계:**
 * - [R2dbcTypeEntity] — 부모 타입 엔티티 (typeId + typeVersion으로 참조)
 *
 * **주의:** inherited 플래그가 true인 속성은 부모 타입에서 상속된 것이며,
 * 타입 저장 시 전체 삭제 후 재삽입 방식으로 갱신된다.
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
    @Column("attribute_type") val attributeType: Json,
    val nullable: Boolean,
    val inherited: Boolean,
    /** JSONB — 읽기 권한 역할 목록의 직렬화된 형태 */
    @Column("read_roles") val readRoles: Json = Json.of("[]"),
    /** JSONB — 쓰기 권한 역할 목록의 직렬화된 형태 */
    @Column("write_roles") val writeRoles: Json = Json.of("[]"),
)
