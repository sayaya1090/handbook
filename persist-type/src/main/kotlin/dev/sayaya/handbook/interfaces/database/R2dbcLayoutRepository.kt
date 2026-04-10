package dev.sayaya.handbook.interfaces.database

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.sayaya.handbook.domain.TypeLayout
import dev.sayaya.handbook.usecase.LayoutRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/** Spring Data R2DBC 자동 구현 인터페이스. type_layouts 테이블에 대한 기본 CRUD + 워크스페이스별 조회를 제공한다. */
interface R2dbcLayoutEntityRepository : ReactiveCrudRepository<R2dbcLayoutEntity, UUID> {
    fun findByWorkspace(workspace: UUID): Flux<R2dbcLayoutEntity>
}

/**
 * [LayoutRepository] 포트의 R2DBC 어댑터.
 *
 * **책임:** 레이아웃 도메인 객체와 R2DBC 엔티티 간 변환, positions JSONB 직렬화/역직렬화를 담당한다.
 *
 * **의존관계:**
 * - [R2dbcLayoutEntityRepository] — Spring Data 기본 CRUD
 * - [ObjectMapper] — positions JSONB 직렬화/역직렬화
 */
class R2dbcLayoutRepositoryAdapter(
    private val repository: R2dbcLayoutEntityRepository,
    private val objectMapper: ObjectMapper,
) : LayoutRepository {

    override fun findByWorkspace(workspace: UUID): Flux<TypeLayout> =
        repository.findByWorkspace(workspace).map { entity ->
            val positions: Map<String, TypeLayout.Position> = entity.positions?.let {
                objectMapper.readValue(it)
            } ?: emptyMap()
            entity.toDomain(positions)
        }

    override fun save(workspace: UUID, layout: TypeLayout): Mono<TypeLayout> {
        val positionsJson = objectMapper.writeValueAsString(layout.positions)
        val entity = R2dbcLayoutEntity.fromDomain(layout, positionsJson)
        return repository.save(entity).map { saved ->
            val positions: Map<String, TypeLayout.Position> = saved.positions?.let {
                objectMapper.readValue(it)
            } ?: emptyMap()
            saved.toDomain(positions)
        }
    }
}
