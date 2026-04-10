package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.TypeLayout
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("type_layouts")
data class R2dbcLayoutEntity(
    @Id val id: UUID,
    val workspace: UUID,
    @Column("effect_date_time") val effectDateTime: Instant,
    @Column("expire_date_time") val expireDateTime: Instant,
    /** positions는 JSONB 컬럼으로 저장 */
    val positions: String?,
) {
    fun toDomain(positionsMap: Map<String, TypeLayout.Position>): TypeLayout = TypeLayout(
        id = id,
        workspace = workspace,
        effectDateTime = effectDateTime,
        expireDateTime = expireDateTime,
        positions = positionsMap,
    )

    companion object {
        fun fromDomain(layout: TypeLayout, positionsJson: String?): R2dbcLayoutEntity = R2dbcLayoutEntity(
            id = layout.id,
            workspace = layout.workspace,
            effectDateTime = layout.effectDateTime,
            expireDateTime = layout.expireDateTime,
            positions = positionsJson,
        )
    }
}
