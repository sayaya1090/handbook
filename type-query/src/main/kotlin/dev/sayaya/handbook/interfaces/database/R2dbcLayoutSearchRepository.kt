package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Position
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
 * [LayoutSearchRepository] 포트의 R2DBC 읽기 전용 어댑터.
 *
 * **책임:** 워크스페이스별 타입 레이아웃 정보를 조회하고, positions JSONB 데이터를 역직렬화하여 도메인 객체로 변환한다.
 *
 * **의존관계:**
 * - [R2dbcLayoutEntityRepository] — `type_layouts` 테이블 조회
 * - [ObjectMapper] — positions JSONB 역직렬화
 */
class R2dbcLayoutSearchRepositoryAdapter(
    private val repository: R2dbcLayoutEntityRepository,
    private val objectMapper: ObjectMapper,
) : LayoutSearchRepository {

    override fun findByWorkspace(workspace: UUID): Flux<TypeLayout> =
        repository.findByWorkspace(workspace).map(::toDomain)

    internal fun toDomain(entity: R2dbcLayoutEntity): TypeLayout {
        val positions: Map<String, Position> = entity.positions?.asString()?.let {
            objectMapper.readValue(it)
        } ?: emptyMap()
        return entity.toDomain(positions)
    }
}
