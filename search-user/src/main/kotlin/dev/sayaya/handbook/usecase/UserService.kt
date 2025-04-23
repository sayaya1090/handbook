package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.User
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.security.Principal
import java.util.*

@Service
class UserService(private val repo: UserRepository) {
    fun find(principal: Principal): Mono<User> {
        return principal.name.let(UUID::fromString).let(repo::find)
    }
}