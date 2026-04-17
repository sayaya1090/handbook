package dev.sayaya.handbook.domain

import java.time.Instant

/**
 * JWT 토큰의 클레임을 나타내는 도메인 객체.
 *
 * **책임:** 토큰 서명 전 클레임 데이터를 구조화한다.
 * [TokenFactory][dev.sayaya.handbook.usecase.TokenFactory]에서 이 객체를 받아 JWT 문자열로 변환한다.
 *
 * **주의:** 시간 필드는 반드시 [Instant] (UTC epoch) 를 사용한다. `LocalDateTime` 은
 * 금지 — pod TZ(Asia/Seoul) 에서 `LocalDateTime.now().toInstant(ZoneOffset.UTC)` 로 변환 시
 * 벽시계를 UTC 로 오인해 9시간 미래 `iat/nbf` 가 찍히는 버그가 2026-04 dev 에서 관찰됨.
 *
 * @property nbf Not Before — 토큰 유효 시작 UTC 시각
 * @property exp Expiration — 토큰 만료 UTC 시각
 * @property iss Issuer — 토큰 발행자
 * @property iat Issued At — 토큰 발행 UTC 시각
 * @property authorities 사용자 권한 목록 (역할 이름)
 * @property name 사용자 표시 이름
 * @property id 사용자 UUID 문자열
 */
data class Token(
    val nbf: Instant,
    val exp: Instant,
    val iss: String,
    val iat: Instant,
    val authorities: List<String>,
    val name: String,
    val id: String,
)
