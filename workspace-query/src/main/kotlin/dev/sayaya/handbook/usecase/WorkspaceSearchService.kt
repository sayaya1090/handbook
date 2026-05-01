package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Workspace
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * 워크스페이스 조회 유스케이스.
 *
 * **책임:** [WorkspaceReadRepository] 로부터 워크스페이스 목록 · 단건을 조회하여 반환한다.
 * 향후 권한 필터(사용자가 속한 그룹의 워크스페이스만), 페이지네이션, 검색 조건 등 확장 지점.
 *
 * **의존관계:**
 * - [WorkspaceReadRepository] — R2DBC 읽기 전용 포트
 *
 * **주의:** 현재는 전체 조회 — 실 운영에서는 호출자 principal 기반 필터링 필수.
 * 후속 반복에서 권한 매트릭스와 결합된다 (auth-expert 와 조율).
 */
@Service
class WorkspaceSearchService(
    private val repository: WorkspaceReadRepository,
) {
    fun list(): Flux<Workspace> = repository.findAll()

    /**
     * principal (사용자 UUID) 기준 워크스페이스 목록.
     *
     * 사용자가 속한 그룹의 워크스페이스만 반환 — 어드민 그룹 포함. workspace-command 가
     * 생성 시 생성자를 admin 그룹에 자동 배정하므로, 방금 만든 워크스페이스도 즉시 응답에
     * 포함된다.
     */
    fun listForUser(sub: UUID): Flux<Workspace> = repository.findByUserSub(sub)

    fun findById(id: UUID): Mono<Workspace> = repository.findById(id)
}
