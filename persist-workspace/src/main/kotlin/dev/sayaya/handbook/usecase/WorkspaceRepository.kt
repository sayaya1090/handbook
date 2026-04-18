package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Workspace
import reactor.core.publisher.Mono
import java.util.UUID

/**
 * 워크스페이스 영속화 포트.
 *
 * **감사 컬럼 주입 규약 (2026-04-18):** `created_by` / `last_modified_by` UUID 는
 * Spring Data R2DBC auditing 이 아닌 **호출 측이 명시 전달** 한다. 이유:
 * - `SecurityContextUuidAuditorConfig` 의 `ReactiveAuditorAware` 는
 *   `Authentication.principal` 이 `String` 또는 `UUID` 일 때만 동작한다. 하지만
 *   현재 구현(`UserAuthentication.getPrincipal()`) 은 `this` 를 반환하므로
 *   auditor 가 empty 를 돌려주고, `@CreatedBy` 필드가 세팅되지 않아 DDL 의
 *   `DEFAULT '00000000-0000-0000-0000-000000000000'` 로 떨어진다.
 * - `group_member.member` 와 동일하게 **명시 주입** 경로로 일관화한다.
 */
interface WorkspaceRepository {
    /**
     * 새 워크스페이스를 저장한다.
     *
     * @param workspace 저장할 워크스페이스 도메인
     * @param creator `created_by` · `last_modified_by` 에 기록할 사용자 UUID
     */
    fun save(workspace: Workspace, creator: UUID): Mono<Workspace>

    /**
     * 기존 워크스페이스를 수정한다.
     *
     * @param workspace 수정할 워크스페이스 도메인
     * @param modifier `last_modified_by` 에 기록할 사용자 UUID
     */
    fun update(workspace: Workspace, modifier: UUID): Mono<Workspace>

    fun delete(id: UUID): Mono<Void>
}
