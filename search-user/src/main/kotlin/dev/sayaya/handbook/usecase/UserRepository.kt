package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.User
import reactor.core.publisher.Mono
import java.util.*

interface UserRepository {
    fun find(id: UUID): Mono<User>
}