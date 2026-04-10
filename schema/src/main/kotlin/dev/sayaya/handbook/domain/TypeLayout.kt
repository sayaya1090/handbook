package dev.sayaya.handbook.domain

import java.io.Serializable
import java.time.Instant
import java.util.*

/**
 * 타입의 캔버스 시각화를 위한 레이아웃 엔티티(Entity).
 *
 * 타입 도메인과 시각화를 분리하여, 타입의 비즈니스 속성과
 * 캔버스 배치 정보를 독립적으로 관리한다.
 *
 * @property id 레이아웃 고유 식별자
 * @property workspace 워크스페이스 ID
 * @property effectDateTime 레이아웃 유효 시작 시각
 * @property expireDateTime 레이아웃 유효 종료 시각
 * @property positions 타입별 캔버스 배치 정보 (타입 ID → 위치·크기)
 */
@JvmRecord
data class TypeLayout(
    val id: UUID,
    val workspace: UUID,
    val effectDateTime: Instant,
    val expireDateTime: Instant,
    val positions: Map<String, Position>,
) : Serializable {
    init {
        require(expireDateTime.isAfter(effectDateTime)) { "Expire date time must be after effect date time" }
    }

    /**
     * 캔버스 위에서 타입이 차지하는 위치와 크기를 나타내는 값 객체.
     */
    @JvmRecord
    data class Position(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int,
    ) : Serializable {
        init {
            require(width > 0) { "Width must be greater than 0. Current value: $width" }
            require(height > 0) { "Height must be greater than 0. Current value: $height" }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as TypeLayout
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
