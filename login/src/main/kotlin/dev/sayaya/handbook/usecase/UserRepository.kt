package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.User
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

interface UserRepository {
    fun findUserByProviderAndAccount(provider: String, account: String): Mono<User>
    fun create(user: User): Mono<User>
    fun updateLastLoginDateTime(id: UUID, lastLoginDateTime: LocalDateTime): Mono<Void>
}