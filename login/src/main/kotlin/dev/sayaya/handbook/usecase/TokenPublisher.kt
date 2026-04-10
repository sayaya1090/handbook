package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.SystemRole
import dev.sayaya.handbook.domain.User
import dev.sayaya.handbook.interfaces.authentication.UserAuthentication
import org.springframework.security.oauth2.core.user.OAuth2User
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

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
            .switchIfEmpty(createUser(provider, principal))
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
