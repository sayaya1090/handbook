package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

@Service
class TokenPublisher(
    private val userRepository: UserRepository
) {
    fun publish(provider: String, principal: OAuth2User): Mono<String> = findUser(provider, principal)
        .delayUntil { userRepository.updateLastLoginDateTime(it.id, LocalDateTime.now()) }
        .switchIfEmpty{ createUser(provider, principal) }
        .map { factory.publish(it) }

    private fun findUser(provider: String, principal: OAuth2User): Mono<User> = userRepository
        .findUserByProviderAndAccount(provider, principal.name)
}