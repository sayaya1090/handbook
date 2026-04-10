package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.SystemRole
import dev.sayaya.handbook.domain.User
import dev.sayaya.handbook.interfaces.authentication.UserAuthentication
import org.springframework.security.oauth2.core.user.OAuth2User
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

/**
 * OAuth2 인증 후 JWT 발급 및 토큰 갱신 유스케이스.
 *
 * **책임:** OAuth2 로그인 성공 시 기존 사용자를 조회하거나 신규 생성한 뒤 JWT를 발급한다.
 * 토큰 갱신 시 사용자 ID로 조회하여 새 JWT를 재발급한다.
 *
 * **의존관계:**
 * - [UserRepository] — 사용자 조회/생성/로그인 시각 갱신
 * - [TokenFactory] — JWT 서명 및 생성
 *
 * **주의:** 신규 사용자는 [SystemRole.USER][dev.sayaya.handbook.domain.SystemRole.USER] 역할로 자동 생성된다.
 * OAuth2 principal에서 "name" 속성이 없으면 account ID를 표시 이름으로 사용한다.
 */
class TokenPublisher(
    private val userRepository: UserRepository,
    private val factory: TokenFactory,
) {
    fun publish(provider: String, principal: OAuth2User): Mono<String> {
        val account = principal.name
        return userRepository.findUserByProviderAndAccount(provider, account)
            .flatMap { user ->
                userRepository.updateLastLoginDateTime(user.id, LocalDateTime.now())
                    .thenReturn(user)
            }
            .switchIfEmpty(Mono.defer { createUser(provider, principal) })
            .map { user -> factory.publish(user) }
    }

    fun validateRefreshToken(authentication: UserAuthentication): Mono<String> {
        val id = authentication.id?.let { UUID.fromString(it) }
            ?: return Mono.error(IllegalArgumentException("User ID is missing"))
        return userRepository.findUserById(id)
            .map { user -> factory.publish(user) }
    }

    private fun createUser(provider: String, principal: OAuth2User): Mono<User> {
        val user = User(
            id = UUID.randomUUID(),
            provider = provider,
            account = principal.name,
            name = principal.getAttribute<String>("name") ?: principal.name,
            roles = mutableListOf(SystemRole.USER),
            lastLoginDateTime = LocalDateTime.now(),
        )
        return userRepository.create(user)
    }
}
