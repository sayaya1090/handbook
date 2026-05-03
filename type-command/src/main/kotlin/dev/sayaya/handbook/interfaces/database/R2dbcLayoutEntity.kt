package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.TypeLayout
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

/**
 * type_layouts 테이블에 매핑되는 R2DBC 엔티티.
 *
 * **책임:** 타입 캔버스 레이아웃(노드 위치 정보)과 DB 행 간의 양방향 변환을 담당한다.
 * positions 컬럼은 JSONB 문자열로 저장되며, JSON 역직렬화는 [R2dbcLayoutRepositoryAdapter]에서 처리한다.
 *
 * **주의:** positions가 null이면 레이아웃이 아직 정의되지 않은 상태이다.
 */
@Table("type_layouts")
data class R2dbcLayoutEntity(
    @Id val id: UUID,
    val workspace: UUID,
    @Column("effect_date_time") val effectDateTime: Instant,
    @Column("expire_date_time") val expireDateTime: Instant,
    /** positions는 JSONB 컬럼으로 저장 */
    val positions: String?,
) {
    fun toDomain(positionsMap: Map<String, dev.sayaya.handbook.domain.Position>): TypeLayout {
        val map = java.lang.reflect.Proxy.newProxyInstance(
            jsinterop.base.JsPropertyMap::class.java.classLoader,
            arrayOf(jsinterop.base.JsPropertyMap::class.java)
        ) { _, method, args ->
            when (method.name) {
                "get" -> positionsMap[args[0] as String]
                "set" -> null
                "forEach" -> null
                else -> null
            }
        } as jsinterop.base.JsPropertyMap<dev.sayaya.handbook.domain.Position>
        return TypeLayout.create(
            id.toString(),
            workspace.toString(),
            effectDateTime.toEpochMilli().toDouble(),
            expireDateTime.toEpochMilli().toDouble(),
            map
        )
    }

    companion object {
        fun fromDomain(layout: TypeLayout, positionsJson: String?): R2dbcLayoutEntity = R2dbcLayoutEntity(
            id = UUID.fromString(layout.id()),
            workspace = UUID.fromString(layout.workspace()),
            effectDateTime = Instant.ofEpochMilli(layout.effectDateTime().toLong()),
            expireDateTime = Instant.ofEpochMilli(layout.expireDateTime().toLong()),
            positions = positionsJson,
        )
    }
}
