package dev.sayaya.handbook.domain

import java.time.Instant
import java.time.LocalDateTime
import java.util.*

/**
 * 사용자 도메인 객체.
 *
 * **책임:** OAuth2 인증된 사용자의 계정 정보와 역할을 보유하며,
 * [toToken]으로 JWT 클레임 객체를 생성한다.
 *
 * **주의:** [roles]는 mutable 리스트로, 현재 모든 사용자는 [SystemRole.USER][dev.sayaya.handbook.domain.SystemRole.USER]로 생성된다.
 *
 * @property provider OAuth2 제공자 (google, github 등)
 * @property account 제공자 내 고유 계정 식별자
 */
data class User(
    val id: UUID,
    val provider: String,
    val account: String,
    val name: String,
    val roles: MutableList<Role> = mutableListOf(),
    val lastLoginDateTime: LocalDateTime? = null,
) {
    /**
     * 도메인 [User] → JWT 서명용 [Token] 변환.
     *
     * - `sub` = 내부 사용자 UUID (영구 식별자)
     * - `id`(jti) = 매 토큰 고유 UUID (호출자가 주입) — 재발행 시에도 사용자 식별자는 불변
     */
    fun toToken(nbf: Instant, exp: Instant, iss: String, iat: Instant, jti: UUID) = Token(
        nbf = nbf,
        exp = exp,
        iss = iss,
        iat = iat,
        authorities = roles.map { it.name },
        name = name,
        sub = id.toString(),
        id = jti.toString(),
    )
}
