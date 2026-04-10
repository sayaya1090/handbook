package dev.sayaya.handbook.interfaces.database

import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

/**
 * Spring Data R2DBC 자동 구현 인터페이스. users 테이블에 대한 기본 CRUD + 커스텀 쿼리를 제공한다.
 *
 * **책임:** OAuth2 제공자+계정 기준 사용자 조회, 마지막 로그인 시각 갱신을 지원한다.
 */
interface R2dbcUserRepository : ReactiveCrudRepository<R2dbcUserEntity, UUID> {
    fun findByProviderAndAccount(provider: String, account: String): Mono<R2dbcUserEntity>

    @Modifying
    @Query("UPDATE users SET last_login_at = :lastLoginAt WHERE id = :id")
    fun updateLastLoginDateTimeById(id: UUID, lastLoginAt: LocalDateTime): Mono<Int>
}
