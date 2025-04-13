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
    fun publish(provider: String, principal: OAuth2User): Mono<String> = findUser(provider, principal)
        .delayUntil { userRepository.updateLastLoginDateTime(it.id, LocalDateTime.now()) }
        .switchIfEmpty(createUser(provider, principal))
        .map { factory.publish(it) }

    private fun findUser(provider: String, principal: OAuth2User): Mono<User> = userRepository
        .findUserByProviderAndAccount(provider, principal.name)
    private fun createUser(provider: String, principal: OAuth2User): Mono<User> {
        val userId = UUID.randomUUID()
        val values = mutableMapOf<String, String>()
        if(principal.attributes["email"]!=null) values["email"] = principal.attributes["email"].toString()
        val user = User(id=userId,  roles = mutableListOf(SystemRole.USER)).apply {
            //if("github".contentEquals(provider))        github = principal.name
            //else if("google".contentEquals(provider))   google = principal.name
            lastLoginDateTime = LocalDateTime.now()
        }
        return userRepository.create(user)
    }
}