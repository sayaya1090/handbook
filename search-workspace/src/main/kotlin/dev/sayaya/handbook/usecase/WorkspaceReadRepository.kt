package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Workspace
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

/**
 * 워크스페이스 read-only 포트 (헥사고날 출력 포트).
 *
 * **책임:** 워크스페이스 엔티티를 읽기 전용으로 조회한다. 삽입/수정/삭제 책임 없음.
 *
 * **구현체:** `interfaces/database/R2dbcWorkspaceReadAdapter`
 *
 * **주의:** persist-workspace 의 `WorkspaceRepository` 와 의도적으로 분리됨.
 * search-workspace 는 read-only PostgreSQL 세션으로 연결되어, 구현체가 SELECT 만
 * 수행 가능하다. 향후 스냅샷/캐시 어댑터로 대체할 여지도 열어둔다.
 */
interface WorkspaceReadRepository {
    fun findAll(): Flux<Workspace>
    fun findById(id: UUID): Mono<Workspace>
}
