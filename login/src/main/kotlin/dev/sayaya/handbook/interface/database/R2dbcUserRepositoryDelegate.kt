package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.User
import dev.sayaya.handbook.usecase.UserRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.time.LocalDateTime
import java.util.*

@Component
class R2dbcUserRepositoryDelegate(private val repo: R2dbcUserRepository): UserRepository {
    override fun findUserByProviderAndAccount(provider: String, account: String): Mono<User> {
        return repo.findByProviderAndAccount(provider, account)
            .map { User(it.id, it.provider, it.account, it.name) }
    }
    override fun create(user: User): Mono<User> = user.toMono().map {
        R2dbcUserEntity(id=it.id, provider = it.provider, account = it.account, name=it.name).apply { lastLoginDateTime = user.lastLoginDateTime }
    }.flatMap(repo::save).map {
        user.apply { lastLoginDateTime = it.lastLoginDateTime }
    }
    override fun updateLastLoginDateTime(id: UUID, lastLoginDateTime: LocalDateTime): Mono<Void> = repo.updateLastLoginDateTimeById(id, lastLoginDateTime).flatMap { updated ->
        if(updated == 1) Mono.empty()
        else Mono.error(IllegalStateException("Update failed, affected rows: $updated"))
    }
}