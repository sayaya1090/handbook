package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.Type
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

/**
 * types 테이블에 매핑되는 R2DBC 엔티티.
 *
 * **책임:** 타입 도메인 객체와 DB 행 간의 양방향 변환을 담당한다.
 * 속성(attributes)은 별도 테이블([R2dbcAttributeEntity])에 저장되므로,
 * [toDomain] 호출 시 속성 리스트를 외부에서 주입받는다.
 *
 * **주의:** id는 타입 이름 문자열이며, version과 함께 복합 비즈니스 키를 형성한다.
 * rev 컬럼은 @Version으로 낙관적 잠금에 사용된다.
 */
@Table("types")
data class R2dbcTypeEntity(
    @Id val id: String,
    val version: String,
    val workspace: UUID,
    @Column("effect_date_time") val effectDateTime: Instant,
    @Column("expire_date_time") val expireDateTime: Instant,
    val description: String?,
    val primitive: Boolean,
    val parent: String?,
    @Version val rev: Long? = null,
) {
    fun toDomain(attributes: List<Attribute> = emptyList()): Type = Type(
        id = id,
        version = version,
        effectDateTime = effectDateTime,
        expireDateTime = expireDateTime,
        description = description,
        primitive = primitive,
        attributes = attributes,
        parent = parent,
        rev = rev,
    )

    companion object {
        fun fromDomain(workspace: UUID, type: Type): R2dbcTypeEntity = R2dbcTypeEntity(
            id = type.id,
            version = type.version,
            workspace = workspace,
            effectDateTime = type.effectDateTime,
            expireDateTime = type.expireDateTime,
            description = type.description,
            primitive = type.primitive,
            parent = type.parent,
        )
    }
}
