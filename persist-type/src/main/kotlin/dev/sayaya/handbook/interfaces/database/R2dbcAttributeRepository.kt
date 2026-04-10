package dev.sayaya.handbook.interfaces.database

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * Spring Data R2DBC 자동 구현 인터페이스. type_attributes 테이블에 대한 CRUD + 커스텀 쿼리를 제공한다.
 *
 * **책임:** 타입 ID/버전 기준 속성 조회, 워크스페이스+타입 ID 기준 일괄 조회,
 * 타입 ID/버전 기준 전체 삭제, 타입 ID/버전/이름 기준 단건 삭제를 지원한다.
 *
 * **주의:** 커스텀 삭제 쿼리는 @Query로 직접 SQL을 작성한다.
 * Spring Data 메서드 이름 기반 파생은 복합 키 삭제를 지원하지 않기 때문이다.
 */
interface R2dbcAttributeEntityRepository : ReactiveCrudRepository<R2dbcAttributeEntity, UUID> {
    fun findByTypeIdAndTypeVersion(typeId: String, typeVersion: String): Flux<R2dbcAttributeEntity>

    fun findByWorkspaceAndTypeIdIn(workspace: UUID, typeIds: Collection<String>): Flux<R2dbcAttributeEntity>

    @Query("DELETE FROM type_attributes WHERE type_id = :typeId AND type_version = :typeVersion")
    fun deleteByTypeIdAndTypeVersion(typeId: String, typeVersion: String): Mono<Void>

    @Query("DELETE FROM type_attributes WHERE type_id = :typeId AND type_version = :typeVersion AND name = :name")
    fun deleteByTypeIdAndTypeVersionAndName(typeId: String, typeVersion: String, name: String): Mono<Void>
}
