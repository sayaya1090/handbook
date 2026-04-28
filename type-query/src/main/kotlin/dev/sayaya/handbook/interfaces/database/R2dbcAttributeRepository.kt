package dev.sayaya.handbook.interfaces.database

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import java.util.UUID

/**
 * Spring Data R2DBC 자동 구현 인터페이스 — `type_attributes` 테이블 읽기 전용 벌크 조회.
 */
interface R2dbcAttributeEntityRepository : ReactiveCrudRepository<R2dbcAttributeEntity, UUID> {
    fun findByWorkspaceAndTypeIdIn(workspace: UUID, typeIds: Collection<String>): Flux<R2dbcAttributeEntity>
}
