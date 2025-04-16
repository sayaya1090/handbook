package dev.sayaya.handbook.`interface`.database

import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

@Repository
interface R2dbcUserRepository: R2dbcRepository<R2dbcUserEntity, UUID> {
    fun findByProviderAndAccount(provider: String, account: String): Mono<R2dbcUserEntity>

    @Modifying
    @Query("UPDATE \"user\" SET last_login_at = :lastLoginAt WHERE id = :id")
    fun updateLastLoginDateTimeById(id: UUID, lastLoginAt: LocalDateTime): Mono<Int>
}