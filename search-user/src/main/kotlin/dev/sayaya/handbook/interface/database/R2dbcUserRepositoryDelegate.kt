package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.User
import dev.sayaya.handbook.usecase.UserRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*

@Component
class R2dbcUserRepositoryDelegate(val repo: R2dbcUserRepository): UserRepository {
    override fun find(id: UUID): Mono<User> = repo.findById(id).map { it.toDomain() }
    private fun R2dbcUserEntity.toDomain(): User = User (
        id = id,
        name = name
    )
}