package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Position
import dev.sayaya.handbook.domain.TypeLayout
import jsinterop.base.JsPropertyMap
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
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
    val positions: io.r2dbc.postgresql.codec.Json?,
    @Version val rev: Long? = null,
) {
    fun toDomain(positionsMap: Map<String, Position>): TypeLayout {
        val map = dev.sayaya.handbook.interfaces.jackson.JsPropertyMapModule.createProxy(positionsMap.toMutableMap())
        val layout = TypeLayout.create(
            id.toString(),
            workspace.toString(),
            effectDateTime.toEpochMilli().toDouble(),
            expireDateTime.toEpochMilli().toDouble(),
            map
        )
        val r = this@R2dbcLayoutEntity.rev
        if (r != null) {
            layout.rev(r)
        }
        return layout
    }
}
