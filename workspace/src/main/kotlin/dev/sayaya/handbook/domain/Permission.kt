package dev.sayaya.handbook.domain

import java.io.Serializable

/**
 * 리소스에 대한 접근 권한을 나타내는 값 객체(Value Object).
 *
 * `resource:action` 형식의 문자열로 표현되며, 와일드카드(`*`)를 지원한다.
 * 예: `workspace:type:create`, `workspace:type:*:view`, `*:*`
 *
 * @property value 권한 문자열 (콜론 구분, 최소 2 세그먼트)
 */
@JvmRecord
data class Permission(val value: String) : Serializable {
    init {
        require(value.isNotBlank()) { "Permission value cannot be blank" }
        val segs = value.split(":")
        require(segs.size >= 2) { "Permission must have at least 2 segments separated by ':'" }
        require(segs.all { it.isNotBlank() }) { "Permission segments cannot be blank" }
    }

    /**
     * 이 Permission이 대상 Permission과 매칭되는지 확인한다.
     * 와일드카드(`*`)는 해당 세그먼트의 모든 값과 매칭된다.
     *
     * 예: `type:*:view`.matches(`type:customer:view`) → true
     */
    fun matches(target: Permission): Boolean {
        val thisSegs = this.value.split(":")
        val targetSegs = target.value.split(":")
        if (thisSegs.size != targetSegs.size) return false
        return thisSegs.zip(targetSegs).all { (a, b) -> a == "*" || a == b }
    }
}
