package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.SystemRole
import dev.sayaya.handbook.domain.User
import dev.sayaya.handbook.usecase.UserRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

@Repository
class R2dbcUserRepositoryDelegate(
    private val repo: R2dbcUserRepository,
) : UserRepository {

    override fun findUserById(id: UUID): Mono<User> {
        return repo.findById(id).map { it.toDomain() }
    }

    override fun findUserByProviderAndAccount(provider: String, account: String): Mono<User> {
        return repo.findByProviderAndAccount(provider, account).map { it.toDomain() }
    }

    override fun create(user: User): Mono<User> {
        val entity = R2dbcUserEntity(
            id = user.id,
            provider = user.provider,
            account = user.account,
            name = user.name,
            lastLoginDateTime = user.lastLoginDateTime,
        ).apply { new = true }
        return repo.save(entity).map { it.toDomain() }
    }

    override fun updateLastLoginDateTime(id: UUID, lastLoginDateTime: LocalDateTime): Mono<Void> {
        return repo.updateLastLoginDateTimeById(id, lastLoginDateTime).then()
    }

    private fun R2dbcUserEntity.toDomain() = User(
        id = getId(),
        provider = provider,
        account = account,
        name = name,
        roles = mutableListOf(SystemRole.USER),
        lastLoginDateTime = lastLoginDateTime,
    )
}
