package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.SystemRole
import dev.sayaya.handbook.domain.User
import dev.sayaya.handbook.usecase.UserRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

/**
 * [UserRepository] 포트의 R2DBC 어댑터.
 *
 * **책임:** R2DBC 엔티티와 도메인 [User] 간 변환을 수행하며,
 * 사용자 조회, 생성, 마지막 로그인 시각 갱신을 구현한다.
 *
 * **의존관계:**
 * - [R2dbcUserRepository] — Spring Data 기본 CRUD + 커스텀 쿼리
 *
 * **주의:** 신규 사용자 생성 시 [SystemRole.USER] 역할이 자동 부여되며,
 * 도메인 변환 시에도 동일 역할이 항상 설정된다.
 */
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
