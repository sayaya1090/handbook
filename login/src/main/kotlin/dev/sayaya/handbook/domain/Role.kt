package dev.sayaya.handbook.domain

/**
 * 사용자 역할을 나타내는 마커 인터페이스.
 *
 * **책임:** 시스템 역할([SystemRole])과 향후 워크스페이스별 역할의 공통 타입을 정의한다.
 * JWT 토큰의 authorities 클레임에 [name]이 포함된다.
 */
interface Role {
    val name: String
}
