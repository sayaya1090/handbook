package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.Type
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

/**
 * `types` 테이블의 **읽기 전용 투영** — search-type 전용.
 *
 * <p>persist-type 의 [R2dbcTypeEntity][dev.sayaya.handbook.interfaces.database.R2dbcTypeEntity]
 * 와 동일 테이블을 공유하지만 검색 경로는 `@Version rev` 를 쓰지 않으므로 읽기 컬럼만 유지한다.
 * 쓰기가 필요하면 persist-type 을 사용한다 (CQRS).</p>
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
    val rev: Long? = null,
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
}
