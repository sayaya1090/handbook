package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.TypeLayout
import dev.sayaya.handbook.usecase.LayoutSearchRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import java.util.UUID

/**
 * Spring Data R2DBC 자동 구현 인터페이스 — `type_layouts` 테이블 읽기 전용 쿼리.
 */
interface R2dbcLayoutEntityRepository : ReactiveCrudRepository<R2dbcLayoutEntity, UUID> {
    fun findByWorkspace(workspace: UUID): Flux<R2dbcLayoutEntity>
}

/**
 * [LayoutSearchRepository] 포트의 R2DBC 읽기 전용 어댑터. positions JSONB 역직렬화를 담당.
 */
class R2dbcLayoutSearchRepositoryAdapter(
    private val repository: R2dbcLayoutEntityRepository,
    private val objectMapper: ObjectMapper,
) : LayoutSearchRepository {

    override fun findByWorkspace(workspace: UUID): Flux<TypeLayout> =
        repository.findByWorkspace(workspace).map { entity ->
            val positions: Map<String, TypeLayout.Position> = entity.positions?.let {
                objectMapper.readValue(it)
            } ?: emptyMap()
            entity.toDomain(positions)
        }
}
