package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.SystemRole
import dev.sayaya.handbook.domain.User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

@Service
class TokenPublisher(
    private val userRepository: UserRepository,
    private val factory: TokenFactory
) {
    fun publish(provider: String, principal: OAuth2User): Mono<String> = userRepository
        .findUserByProviderAndAccount(provider, principal.name)
        .delayUntil { userRepository.updateLastLoginDateTime(it.id, LocalDateTime.now()) }
        .switchIfEmpty(Mono.defer {
            createUser(provider, principal)
        }).map { factory.publish(it) }

    private fun createUser(provider: String, principal: OAuth2User): Mono<User> {
        val userId = UUID.randomUUID()
        val user = User(id=userId, provider = provider, account = principal.name, name = "", roles = mutableListOf(SystemRole.USER)).apply {
            lastLoginDateTime = LocalDateTime.now()
        }
        return userRepository.create(user)
    }
}