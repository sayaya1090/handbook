package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.User
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

/**
 * 사용자 영속화 포트 (헥사고날 아키텍처 출력 포트).
 *
 * **책임:** 사용자의 ID/제공자+계정 기준 조회, 생성, 마지막 로그인 시각 갱신을 정의한다.
 *
 * **의존관계:**
 * - [R2dbcUserRepositoryDelegate][dev.sayaya.handbook.interfaces.database.R2dbcUserRepositoryDelegate] — R2DBC 구현체
 */
interface UserRepository {
    fun findUserById(id: UUID): Mono<User>
    fun findUserByProviderAndAccount(provider: String, account: String): Mono<User>
    fun create(user: User): Mono<User>
    fun updateLastLoginDateTime(id: UUID, lastLoginDateTime: LocalDateTime): Mono<Void>
}
