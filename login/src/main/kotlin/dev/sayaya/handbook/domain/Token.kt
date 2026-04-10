package dev.sayaya.handbook.domain

import java.time.LocalDateTime

/**
 * JWT 토큰의 클레임을 나타내는 도메인 객체.
 *
 * **책임:** 토큰 서명 전 클레임 데이터를 구조화한다.
 * [TokenFactory][dev.sayaya.handbook.usecase.TokenFactory]에서 이 객체를 받아 JWT 문자열로 변환한다.
 *
 * @property nbf Not Before — 토큰 유효 시작 시각
 * @property exp Expiration — 토큰 만료 시각
 * @property iss Issuer — 토큰 발행자
 * @property iat Issued At — 토큰 발행 시각
 * @property authorities 사용자 권한 목록 (역할 이름)
 * @property name 사용자 표시 이름
 * @property id 사용자 UUID 문자열
 */
data class Token(
    val nbf: LocalDateTime,
    val exp: LocalDateTime,
    val iss: String,
    val iat: LocalDateTime,
    val authorities: List<String>,
    val name: String,
    val id: String,
)
