package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.TypeLayout
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

/**
 * `type_layouts` 테이블의 **읽기 전용 투영** — type-query 전용.
 * positions 는 JSONB(String) 로 읽어 adapter 가 ObjectMapper 로 역직렬화.
 */
@Table("type_layouts")
data class R2dbcLayoutEntity(
    @Id val id: UUID,
    val workspace: UUID,
    @Column("effect_date_time") val effectDateTime: Instant,
    @Column("expire_date_time") val expireDateTime: Instant,
    val positions: String?,
) {
    fun toDomain(positionsMap: Map<String, TypeLayout.Position>): TypeLayout = TypeLayout(
        id = id,
        workspace = workspace,
        effectDateTime = effectDateTime,
        expireDateTime = expireDateTime,
        positions = positionsMap,
    )
}
