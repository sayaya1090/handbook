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

    /**
     * 사용자(UUID = JWT `sub`) 가 멤버로 속한 그룹의 워크스페이스를 반환한다.
     *
     * **정의:** `group_member.member = :sub` 인 레코드가 하나라도 있는 워크스페이스 (DISTINCT).
     * 어드민 그룹 포함 — persist-workspace 는 워크스페이스 생성 시 생성자를 자동으로 admin
     * 그룹에 배정한다.
     *
     * **권한 필터링 경로:** `GET /workspaces` 소비자가 principal.sub 기준으로 필터링한
     * 목록만 돌려받도록 한다 (모든 워크스페이스 노출 금지).
     */
    fun findByUserSub(sub: UUID): Flux<Workspace>
}
