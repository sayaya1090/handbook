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
 * ### sub 와 id(jti) 의 역할 구분 (Phase 1a — 2026-04-18)
 * - `sub` : **사용자 식별자** (내부 `user.id` UUID). 재발급 시에도 불변.
 *   서비스 간 소비자(workspace-command 등)는 항상 이 값으로 `user_id` 를 참조한다.
 * - `id`  : **토큰 식별자** (`jti`). 매 토큰 발행마다 `UUID.randomUUID()` 로 새로 생성된다.
 *   감사/블랙리스트/일회성 식별용.
 *
 * 과거(~2026-04-17) 에는 `jti` 에 사용자 UUID 를 직접 심었으나 `jti` 가 토큰별 고유값이
 * 아닌 사용자 고정값이 되는 의미 오염과, 소비자가 `principal.id`(= jti) 를 사용자 ID 로
 * 취급해 토큰 재발행 격리가 깨지는 회귀(`group_member.member=jti` 현상) 의 원인이 되었다.
 *
 * @property nbf Not Before — 토큰 유효 시작 UTC 시각
 * @property exp Expiration — 토큰 만료 UTC 시각
 * @property iss Issuer — 토큰 발행자
 * @property iat Issued At — 토큰 발행 UTC 시각
 * @property authorities 사용자 권한 목록 (역할 이름)
 * @property name 사용자 표시 이름
 * @property sub 사용자 UUID 문자열 (영구 식별자 — JWT `sub` 클레임)
 * @property id 토큰 고유 식별자 UUID 문자열 (JWT `jti` 클레임, 매 발행마다 고유)
 */
data class Token(
    val nbf: Instant,
    val exp: Instant,
    val iss: String,
    val iat: Instant,
    val authorities: List<String>,
    val name: String,
    val sub: String,
    val id: String,
)
